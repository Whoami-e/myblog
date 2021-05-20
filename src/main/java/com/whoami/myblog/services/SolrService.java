package com.whoami.myblog.services;

import com.whoami.myblog.entity.Article;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;

public interface SolrService {
    ResponseResult doSearch(String keyword, Page page, String categoryId, Integer sort);

    void addArticle(Article article);

    void updateArticle(Article article, String articleId);

    void deleteArticle(String articleId);
}
