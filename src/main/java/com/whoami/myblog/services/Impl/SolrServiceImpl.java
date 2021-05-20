package com.whoami.myblog.services.Impl;

import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.whoami.myblog.entity.Article;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.PageList;
import com.whoami.myblog.entity.SearchResult;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.SolrService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.TextUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vladsch.flexmark.util.ast.Node;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SolrServiceImpl implements SolrService {

    @Autowired
    private SolrClient solrClient;

    @Override
    public ResponseResult doSearch(String keyword, Page page, String categoryId, Integer sort) {

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(page.getPageSize());
        solrQuery.setStart((page.getPageNum() - 1) * page.getPageSize());

        //设置搜索条件
        //关键字
        solrQuery.set("df", "search_item");
        //条件过滤
        if (TextUtils.isEmpty(keyword)) {
            solrQuery.set("q", "*");
        } else {
            solrQuery.set("q", keyword);
        }
        //排序 根据时间升序（1）和降序（2），根据浏览量的升序（3）和降序（4）
        if (sort != null) {
            if (sort == 1) {
                solrQuery.setSort("blog_create_time", SolrQuery.ORDER.asc);
            } else if (sort == 2) {
                solrQuery.setSort("blog_create_time", SolrQuery.ORDER.desc);
            } else if (sort == 3) {
                solrQuery.setSort("blog_view_count", SolrQuery.ORDER.asc);
            } else if (sort == 4) {
                solrQuery.setSort("blog_view_count", SolrQuery.ORDER.desc);
            }
        }
        //分类
        if (!TextUtils.isEmpty(categoryId)) {
            solrQuery.setFilterQueries("blog_category_id:" + categoryId);
        }
        //关键字高亮
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("blog_title,blog_content");
        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        solrQuery.setHighlightFragsize(500);
        //设置返回字段
        solrQuery.addField("id,blog_content,blog_create_time,blog_labels,blog_url,blog_title,blog_view_count");
        //搜索
        try {
            QueryResponse result = solrClient.query(solrQuery);
            //获取高亮信息
            Map<String, Map<String, List<String>>> highlighting = result.getHighlighting();
            //把数据转成Bean类
            List<SearchResult> resultList = result.getBeans(SearchResult.class);
            for (SearchResult item : resultList) {
                Map<String, List<String>> stringListMap = highlighting.get(item.getId());
                List<String> blogContent = stringListMap.get("blog_content");
                if (blogContent != null) {
                    item.setBlogContent(blogContent.get(0));
                }
                List<String> blogTitle = stringListMap.get("blog_title");
                if (blogTitle != null) {
                    item.setBlogTitle(blogTitle.get(0));
                }
            }

            long numFound = result.getResults().getNumFound();
            PageList<SearchResult> pageList = new PageList<>(page.getPageNum(),numFound,page.getPageSize());
            pageList.setContents(resultList);

            return ResponseResult.SUCCESS("搜索成功").setData(pageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILED("搜索失败，请稍后重试！");
    }

    public void addArticle(Article article) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", article.getId());
        doc.addField("blog_view_count", article.getViewCount());
        doc.addField("blog_title", article.getTitle());
        //对内容进行处理，去掉标签，提取出纯文本
        //第一种是由markdown写的内容--->type = 1
        //第二种是符文本内容 === > type = 0
        //如果type === 1 ===> 转成html
        //再由html === > 纯文本
        //如果type == 0 == > 纯文本
        String type = article.getType();
        String html;
        if (Constants.Article.TYPE_MARKDOWN.equals(type)) {
            //转成html
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
        //到这里,不管原来是什么,现在都是Html
        //html== > text
        String content = Jsoup.parse(html).text();
        doc.addField("blog_content", content);
        doc.addField("blog_create_time", article.getCreateTime());
        doc.addField("blog_labels", article.getLabels());
        doc.addField("blog_url", "/article/" + article.getId());
        doc.addField("blog_category_id", article.getCategoryId());
        try {
            solrClient.add(doc);
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateArticle(Article article, String articleId) {
        article.setId(articleId);
        this.addArticle(article);
    }

    @Override
    public void deleteArticle(String articleId) {
        try {
            solrClient.deleteById(articleId);
            solrClient.commit();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
