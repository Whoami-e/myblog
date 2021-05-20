package com.whoami.myblog.controller.portal;

import com.whoami.myblog.entity.Comment;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/comment")
public class CommentPortalApi {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论
     * @param comment
     * @return
     */
    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult postComment(@RequestBody Comment comment){
        return commentService.postComment(comment);
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return
     */
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId")String commentId){
        return commentService.deleteCommentById(commentId);
    }

    /**
     * 获取评论列表
     * @param articleId
     * @return
     */
    @GetMapping("/list/{articleId}/{pageNum}/{pageSize}")
    public ResponseResult listCommend(@PathVariable("articleId")String articleId, @PathVariable("pageNum")int pageNum, @PathVariable("pageSize")int pageSize){
        Page page = new Page(pageNum,pageSize);
        return commentService.listCommentByArticleId(page,articleId);
    }
}
