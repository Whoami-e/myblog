package com.whoami.myblog.controller.admin;

import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
public class CommentApi {

    @Autowired
    private CommentService commentService;

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId")String commentId){
        return commentService.deleteCommentById(commentId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{pageNum}/{pageSize}")
    public ResponseResult listComment(@PathVariable("pageNum") int pageNum,@PathVariable("pageSize") int pageSize){
        Page page = new Page(pageNum,pageSize);
        return commentService.listComment(page);
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/top/{commentId}")
    public ResponseResult topComment(@PathVariable("commentId")String commentId){
        return commentService.topComment(commentId);
    }
}
