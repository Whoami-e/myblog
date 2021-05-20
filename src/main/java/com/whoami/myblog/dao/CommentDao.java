package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentDao {
    int deleteByPrimaryKey(String id);

    int insert(Comment comment);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);

    int deleteByArticleId(String articleId);

    List<Comment> selectByArticleId(String articleId);

    List<Comment> selectAll();
}