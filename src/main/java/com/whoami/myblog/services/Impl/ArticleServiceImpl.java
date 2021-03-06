package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.whoami.myblog.dao.*;
import com.whoami.myblog.entity.*;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ArticleService;
import com.whoami.myblog.services.SolrService;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.RedisUtil;
import com.whoami.myblog.utils.SnowFlakeIdWorker;
import com.whoami.myblog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ArticleNoContentDao articleNoContentDao;

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private Random random;

    @Autowired
    private LabelsDao labelsDao;

    @Autowired
    private SolrService solrService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    @Autowired
    private NotesDao notesDao;


    @Override
    public ResponseResult postArticle(Article article) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        User user = userService.checkUser(request, response);
        if (user == null) {
            return ResponseResult.NOT_LOGIN();
        }

        String title = article.getTitle();
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("???????????????????????????");
        }

        String state = article.getState();
        if (!Constants.Article.STATE_PUBLISH.equals(state) && !Constants.Article.STATE_DRAFT.equals(state)) {
            return ResponseResult.FAILED("??????????????????");
        }

        String type = article.getType();
        if (TextUtils.isEmpty(type)) {
            return ResponseResult.FAILED("???????????????????????????");
        }
        if (!"0".equals(type) && !"1".equals(type)) {
            return ResponseResult.FAILED("??????????????????");
        }

        //??????????????????????????????????????????????????????
        if (Constants.Article.STATE_PUBLISH.equals(state)) {

            if (title.length() > Constants.Article.TITLE_MAX_LENGTH) {
                return ResponseResult.FAILED("???????????????????????????" + Constants.Article.TITLE_MAX_LENGTH + "?????????");
            }
            String content = article.getContent();
            if (TextUtils.isEmpty(content)) {
                return ResponseResult.FAILED("???????????????????????????");
            }

            String summary = article.getSummary();
            if (TextUtils.isEmpty(summary)) {
                return ResponseResult.FAILED("???????????????????????????");
            }
            if (summary.length() > Constants.Article.SUMMARY_MAX_LENGTH) {
                return ResponseResult.FAILED("?????????????????????" + Constants.Article.SUMMARY_MAX_LENGTH + "?????????");
            }
            String labels = article.getLabels();
            // System.out.println("labels==>" + labels);
            if (TextUtils.isEmpty(labels)) {
                return ResponseResult.FAILED("???????????????????????????");
            }
        }

        String articleId = article.getId();
        if (TextUtils.isEmpty(articleId)) {
            //???????????????????????????
            article.setId(snowFlakeIdWorker.nextId() + "");
            article.setCreateTime(new Date());
        }
//        else {
//            //????????????
//            Article articleFromDb = articleDao.selectByPrimaryKey(articleId);
//            if (Constants.Article.STATE_PUBLISH.equals(articleFromDb.getState()) && Constants.Article.STATE_DRAFT.equals(state)) {
//                //????????????????????????????????????????????????
//                return ResponseResult.FAILED("????????????????????????????????????");
//            }
//        }

        article.setUserName(user.getUserName());
        article.setUserAvatar(user.getAvatar());
        article.setUpdateTime(new Date());
        article.setUserId(user.getId());
        if (Constants.Article.STATE_DRAFT.equals(state)) {
            articleDao.updateByPrimaryKeySelective(article);
        }
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            articleDao.insert(article);
            solrService.addArticle(article);
        }
        this.setupLabels(article.getLabels());

        redisUtil.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);

        return ResponseResult.SUCCESS(Constants.Article.STATE_DRAFT.equals(state) ? "??????????????????" : "??????????????????").setData(article.getId());
    }

    private void setupLabels(String labels) {
        List<String> labelList = new ArrayList<>();
        if (labels.contains("-")) {
            labelList.addAll(Arrays.asList(labels.split("-")));
        } else {
            labelList.add(labels);
        }

        //???????????????
        for (String label : labelList) {
//            Labels targetLabel = labelsDao.selectByName(label);
//            if (targetLabel == null) {
//                targetLabel = new Labels();
//                targetLabel.setId(snowFlakeIdWorker.nextId() + "");
//                targetLabel.setCount(0);
//                targetLabel.setName(label);
//                targetLabel.setCreateTime(new Date());
//            }
//            long count = targetLabel.getCount();
//            targetLabel.setCount(++count);
//            targetLabel.setUpdateTime(new Date());
            int result = labelsDao.updateByLabelName(label);
            if (result == 0) {
                Labels targetLabel = new Labels();
                targetLabel.setId(snowFlakeIdWorker.nextId() + "");
                targetLabel.setCount(1);
                targetLabel.setName(label);
                targetLabel.setCreateTime(new Date());
                targetLabel.setUpdateTime(new Date());
                labelsDao.insert(targetLabel);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param page       ??????
     * @param keyword    ??????????????????
     * @param categoryId ??????ID
     * @param state      ?????????0??????????????????1???????????????2???????????????3???????????????
     * @return
     */
    @Override
    public ResponseResult listArticles(Page page, String keyword, String categoryId, String state) {
        Integer pageNum = page.getPageNum();
        Integer pageSize = page.getPageSize();

//        String articleListJson = (String) redisUtil.get(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
//        boolean isSearch = !TextUtils.isEmpty(keyword) || !TextUtils.isEmpty(categoryId) || !TextUtils.isEmpty(state);
//        System.out.println(articleListJson);
//        if (!TextUtils.isEmpty(articleListJson) && pageNum == 1 && !isSearch) {
//            PageInfo<ArticleNoContent> result = gson.fromJson(articleListJson, new TypeToken<PageInfo<ArticleNoContent>>() {
//            }.getType());
//            System.out.println("article list from redis ....");
//            return ResponseResult.SUCCESS("????????????").setData(result);
//        }

        PageHelper.startPage(pageNum, pageSize);
        List<ArticleNoContent> articleList = articleNoContentDao.selectAll(keyword, categoryId, state);

//        if (pageNum == 1 && !isSearch) {
//            redisUtil.set(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE, gson.toJson(new PageInfo<>(articleList)), Constants.TimeValue.MIN_15);
//        }

        return ResponseResult.SUCCESS("????????????").setData(new PageInfo<>(articleList));
    }

    @Override
    public ResponseResult getArticleForAdmin(String articleId) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        Article article = articleDao.selectByPrimaryKey(articleId);
        if (article == null) {
            return ResponseResult.FAILED("???????????????");
        }

        return ResponseResult.SUCCESS("??????????????????").setData(article);
    }

    @Override
    public ResponseResult getArticle(String articleId) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String articleJson = (String) redisUtil.get(Constants.Article.KEY_ARTICLE_CACHE + articleId);
        if (!TextUtils.isEmpty(articleJson)) {
            Article article = gson.fromJson(articleJson, Article.class);
            redisUtil.incr(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId, 1);
            return ResponseResult.SUCCESS("??????????????????").setData(article);
        }

        Article article = articleDao.selectByPrimaryKey(articleId);
        if (article == null) {
            return ResponseResult.FAILED("???????????????");
        }

        // ??????token??????????????????
        User user = userService.checkUser(request, response);

        String state = article.getState();
        if (Constants.Article.STATE_PUBLISH.equals(state) || Constants.Article.STATE_TOP.equals(state)) {

            String html;
            // ?????????????????? markdown???HTML
            if (Constants.Article.TYPE_MARKDOWN.equals(article.getType())) {
                //??????html
                // markdown to html
                MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        JekyllTagExtension.create(),
                        TocExtension.create(),
                        SimTocExtension.create()
                ));
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                Node document = parser.parse(article.getContent());
                html = renderer.render(document);
            } else {
                html = article.getContent();
            }

            //??????????????????
            String articleStr = gson.toJson(article);
            Article newArticle = gson.fromJson(articleStr, Article.class);
            newArticle.setContent(html);


            //???????????????????????????Redis???
            redisUtil.set(Constants.Article.KEY_ARTICLE_CACHE + articleId, gson.toJson(newArticle), Constants.TimeValue.MIN_5);
            String viewCount = (String) redisUtil.get(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId);
            if (TextUtils.isEmpty(viewCount)) {
                long newCount = article.getViewCount() + 1;
                redisUtil.set(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId, String.valueOf(newCount));
            } else {
                long newCount = redisUtil.incr(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId, 1);
                article.setViewCount(newCount);
                articleDao.updateByPrimaryKeySelective(article);
                //??????solr
                solrService.updateArticle(article, articleId);
            }


            return ResponseResult.SUCCESS("??????????????????").setData(newArticle);
        }

        // ????????????????????????????????????????????????????????????

        String roles = user.getRoles();
        if (user == null || !Constants.User.ROLE_ADMIN.equals(roles)) {
            return ResponseResult.PERMISSION_DENIED();
        }
        return ResponseResult.SUCCESS("??????????????????").setData(article);
    }

    @Override
    public ResponseResult updateArticle(Article article, String articleId) {
        Article articleFromDb = articleDao.selectByPrimaryKey(articleId);

        if (articleFromDb == null) {
            return ResponseResult.FAILED("???????????????");
        }

        //????????????????????????????????????
        String title = article.getTitle();
        if (!TextUtils.isEmpty(title)) {
            articleFromDb.setTitle(title);
        }
        String summary = article.getSummary();
        if (!TextUtils.isEmpty(summary)) {
            articleFromDb.setSummary(summary);
        }
        String content = article.getContent();
        if (!TextUtils.isEmpty(content)) {
            articleFromDb.setContent(content);
        }
        String labels = article.getLabels();
        if (!TextUtils.isEmpty(labels)) {
            articleFromDb.setLabels(labels);
        }
        String categoryId = article.getCategoryId();
        if (!TextUtils.isEmpty(categoryId)) {
            articleFromDb.setCategoryId(categoryId);
        }
        String cover = article.getCover();
        if (!TextUtils.isEmpty(cover)) {
            articleFromDb.setCover(cover);
        }
        String state = article.getState();
        if (!TextUtils.isEmpty(state)) {
            articleFromDb.setState(state);
        }


        articleFromDb.setUpdateTime(new Date());
        articleDao.updateByPrimaryKeySelective(articleFromDb);
        redisUtil.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
        return ResponseResult.SUCCESS("??????????????????");
    }

    @Override
    public ResponseResult deleteArticle(String articleId) {
        commentDao.deleteByArticleId(articleId);
        int i = articleDao.deleteByPrimaryKey(articleId);
        if (i > 0) {
            redisUtil.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
            redisUtil.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
            solrService.deleteArticle(articleId);
            return ResponseResult.SUCCESS("??????????????????");
        }
        return ResponseResult.FAILED("???????????????");
    }

    @Override
    public ResponseResult deleteArticleByState(String articleId) {
        Article article1 = articleDao.selectByPrimaryKey(articleId);
        if ("2".equals(article1.getState())) {
            return ResponseResult.FAILED("????????????????????????????????????????????????????????????");
        }
        if (article1 == null) {
            return ResponseResult.FAILED("???????????????");
        }
        Article article = new Article();
        article.setState(Constants.Article.STATE_DELETE);
        article.setId(articleId);
        int i = articleDao.updateByPrimaryKeySelective(article);
        if (i > 0) {
            redisUtil.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
            redisUtil.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
            solrService.deleteArticle(articleId);
            return ResponseResult.SUCCESS("??????????????????");
        }
        return ResponseResult.FAILED("???????????????");
    }

    @Override
    public ResponseResult topArticle(String articleId) {
        Article article1 = articleDao.selectByPrimaryKey(articleId);
        if (article1 == null) {
            return ResponseResult.FAILED("???????????????");
        }
        String state = article1.getState();
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            Article article = new Article();
            article.setState(Constants.Article.STATE_TOP);
            article.setId(articleId);
            int i = articleDao.updateByPrimaryKeySelective(article);
            if (i > 0) {
                return ResponseResult.SUCCESS("??????????????????");
            }
        }
        if (Constants.Article.STATE_TOP.equals(state)) {
            Article article = new Article();
            article.setState(Constants.Article.STATE_PUBLISH);
            article.setId(articleId);
            int i = articleDao.updateByPrimaryKeySelective(article);
            if (i > 0) {
                return ResponseResult.SUCCESS("????????????????????????");
            }
        }

        return ResponseResult.FAILED("??????????????????");
    }

    @Override
    public ResponseResult listTopArticle() {
        List<ArticleNoContent> articleList = articleNoContentDao.selectAll("", "", Constants.Article.STATE_TOP);
        return ResponseResult.SUCCESS("????????????????????????").setData(articleList);
    }

    @Override
    public ResponseResult listRecommendArticle(String articleId, int size) {
        if (size < Constants.Page.MIN_SIZE) {
            size = Constants.Page.MIN_SIZE;
        }
        Article article = articleDao.selectByPrimaryKey(articleId);
        String labels = article.getLabels();
        List<String> labelList = new ArrayList<>();
        if (!labels.contains("-")) {
            labelList.add(labels);
        } else {
            labelList.addAll(Arrays.asList(labels.split("-")));
        }
        String label = labelList.get(random.nextInt(labelList.size()));
        // System.out.println("LABEL ==>" + label);
        List<ArticleNoContent> articleByLabel = articleNoContentDao.selectArticleByLabel(label, articleId, size);

        if (articleByLabel.size() < size) {
            int dxSize = size - articleByLabel.size();
            List<ArticleNoContent> dxList = articleNoContentDao.listArticleBySize(articleId, dxSize);
            articleByLabel.addAll(dxList);
        }
        return ResponseResult.SUCCESS("????????????????????????").setData(articleByLabel);
    }

    @Override
    public ResponseResult listArticlesByLabel(Page page, String label) {
        Integer pageNum = page.getPageNum();
        if (pageNum < Constants.Page.DEFAULT_PAGE) {
            pageNum = Constants.Page.DEFAULT_PAGE;
        }
        Integer pageSize = page.getPageSize();
        if (pageSize < Constants.Page.MIN_SIZE) {
            pageSize = Constants.Page.MIN_SIZE;
        }
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleNoContent> articleNoContentList = articleNoContentDao.selectArticleListByLabel(label, Constants.Article.STATE_PUBLISH, Constants.Article.STATE_TOP);
        return ResponseResult.SUCCESS("????????????????????????").setData(new PageInfo<>(articleNoContentList));
    }

    @Override
    public ResponseResult listLabels(int size) {
        if (size < Constants.Page.MIN_SIZE) {
            size = Constants.Page.MIN_SIZE;
        }
        PageHelper.startPage(1, size);
        List<Labels> labels = labelsDao.selectAll();
        return ResponseResult.SUCCESS("??????????????????").setData(new PageInfo<>(labels));
    }

    @Override
    public ResponseResult postNote(Notes notes) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        User user = userService.checkUser(request, response);
        if (user == null) {
            return ResponseResult.NOT_LOGIN();
        }
        // System.out.println(notes);

        String content = notes.getContent();
        // System.out.println("Note Content ==> " + content);
        if (TextUtils.isEmpty(content)) {
            return ResponseResult.FAILED("????????????????????????");
        }
        notes.setId(snowFlakeIdWorker.nextId() + "");
        notes.setCreateTime(new Date());

        notesDao.insert(notes);

        return ResponseResult.SUCCESS("?????????????????????");

    }

    @Override
    public ResponseResult listNotes() {

        PageHelper.startPage(1, 5);
        List<Notes> notes = notesDao.listNotes();
        return ResponseResult.SUCCESS("??????????????????").setData(new PageInfo<>(notes));
    }

    @Override
    public ResponseResult deleteNote(String noteId) {
        Notes notes = notesDao.selectByPrimaryKey(noteId);
        if (notes == null) {
            return ResponseResult.FAILED("?????????????????????");
        }

        int i = notesDao.deleteByPrimaryKey(noteId);
        return ResponseResult.SUCCESS("?????????????????????");
    }
}
