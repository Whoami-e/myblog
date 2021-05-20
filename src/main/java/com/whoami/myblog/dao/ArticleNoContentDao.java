package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Article;
import com.whoami.myblog.entity.ArticleNoContent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleNoContentDao {
    int deleteByPrimaryKey(String id);

    int insert(ArticleNoContent articleNoContent);

    int insertSelective(ArticleNoContent articleNoContent);

    ArticleNoContent selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ArticleNoContent articleNoContent);

    int updateByPrimaryKey(ArticleNoContent articleNoContent);

    List<ArticleNoContent> selectAll(@Param("keyword") String keyword, @Param("categoryId")  String categoryId, @Param("state")  String state);

    List<ArticleNoContent> selectArticleByLabel(@Param("label") String label, @Param("id") String articleId, @Param("size") int size);

    List<ArticleNoContent> listArticleBySize(@Param("id") String id, @Param("size") int dxSize);

    List<ArticleNoContent> selectArticleListByLabel(@Param("label") String label, @Param("statePublish") String statePublish, @Param("stateTop") String stateTop);
}