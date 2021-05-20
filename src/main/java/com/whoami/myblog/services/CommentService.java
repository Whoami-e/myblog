package com.whoami.myblog.services;

import com.whoami.myblog.entity.Comment;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;

public interface CommentService {
    ResponseResult postComment(Comment comment);

    ResponseResult listCommentByArticleId(Page page, String articleId);

    ResponseResult deleteCommentById(String commentId);

    ResponseResult listComment(Page page);

    ResponseResult topComment(String commentId);
}
