package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.whoami.myblog.dao.ArticleNoContentDao;
import com.whoami.myblog.dao.CommentDao;
import com.whoami.myblog.entity.*;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.CommentService;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private ArticleNoContentDao articleNoContentDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    @Override
    public ResponseResult postComment(Comment comment) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        User checkUser = userService.checkUser(request, response);
        if (checkUser == null) {
            return ResponseResult.NOT_LOGIN();
        }

        String articleId = comment.getArticleId();
        if (TextUtils.isEmpty(articleId)) {
            return ResponseResult.FAILED("文章ID不可以为空");
        }

        ArticleNoContent articleNoContent = articleNoContentDao.selectByPrimaryKey(articleId);
        if (articleNoContent == null) {
            return ResponseResult.FAILED("文章不存在");
        }

        String content = comment.getContent();
        if (TextUtils.isEmpty(content)) {
            return ResponseResult.FAILED("评论内容不可以为空");
        }

        comment.setId(snowFlakeIdWorker.nextId() + "");
        comment.setUserName(checkUser.getUserName());
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());
        comment.setUserAvatar(checkUser.getAvatar());
        comment.setUserId(checkUser.getId());

        commentDao.insert(comment);

        redisUtil.del(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + comment.getArticleId());

        return ResponseResult.SUCCESS("评论成功");
    }

    @Override
    public ResponseResult listCommentByArticleId(Page page, String articleId) {
        Integer pageNum = page.getPageNum();
        if (pageNum < Constants.Page.DEFAULT_PAGE) {
            pageNum = Constants.Page.DEFAULT_PAGE;
        }
        Integer pageSize = page.getPageSize();
        if (pageSize < Constants.Page.MIN_SIZE) {
            pageSize = Constants.Page.MIN_SIZE;
        }

        String cacheJson = (String) redisUtil.get(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId);
        if (!TextUtils.isEmpty(cacheJson) && pageNum == 1) {
            PageInfo<Comment> result = gson.fromJson(cacheJson, new TypeToken<PageInfo<Comment>>() {
            }.getType());
            // System.out.println("comment list from redis ....");
            return ResponseResult.SUCCESS("文章评论列表获取成功").setData(result);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Comment> comments = commentDao.selectByArticleId(articleId);

//        PageList<Comment> result = new PageList<>();
//        result.parsePage(new PageInfo<>(comments));

        if (pageNum == 1) {
            redisUtil.set(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId, gson.toJson(new PageInfo<>(comments)), Constants.TimeValue.MIN_5);
        }

        return ResponseResult.SUCCESS("文章评论列表获取成功").setData(new PageInfo<>(comments));
    }

    @Override
    public ResponseResult deleteCommentById(String commentId) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        User checkUser = userService.checkUser(request, response);
        if (checkUser == null) {
            return ResponseResult.NOT_LOGIN();
        }

        Comment comment = commentDao.selectByPrimaryKey(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在");
        }

        if (Constants.User.ROLE_ADMIN.equals(checkUser.getRoles()) || checkUser.getId().equals(comment.getUserId())) {
            commentDao.deleteByPrimaryKey(commentId);
            return ResponseResult.SUCCESS("评论删除成功");
        } else {
            return ResponseResult.PERMISSION_DENIED();
        }
    }

    @Override
    public ResponseResult listComment(Page page) {

        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<Comment> commentList = commentDao.selectAll();
        return ResponseResult.SUCCESS("查询成功").setData(new PageInfo<>(commentList));
    }

    @Override
    public ResponseResult topComment(String commentId) {
        Comment comment = commentDao.selectByPrimaryKey(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在");
        }
        String state = comment.getState();
        if (Constants.Comment.STATE_PUBLISH.equals(state)) {
            comment.setState(Constants.Comment.STATE_TOP);
            commentDao.updateByPrimaryKeySelective(comment);
            return ResponseResult.SUCCESS("置顶成功");
        } else if (Constants.Comment.STATE_TOP.equals(state)) {
            comment.setState(Constants.Comment.STATE_PUBLISH);
            commentDao.updateByPrimaryKeySelective(comment);
            return ResponseResult.SUCCESS("取消置顶成功");
        } else {
            return ResponseResult.FAILED("评论状态非法");
        }
    }
}
