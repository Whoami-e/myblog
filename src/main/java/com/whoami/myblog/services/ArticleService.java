package com.whoami.myblog.services;

import com.whoami.myblog.entity.Article;
import com.whoami.myblog.entity.Notes;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;

public interface ArticleService {
    ResponseResult postArticle(Article article);

    ResponseResult listArticles(Page page, String keyword, String categoryId, String state);

    ResponseResult getArticle(String articleId);

    ResponseResult getArticleForAdmin(String articleId);

    ResponseResult updateArticle(Article article, String articleId);

    ResponseResult deleteArticle(String articleId);

    ResponseResult deleteArticleByState(String articleId);

    ResponseResult topArticle(String articleId);

    ResponseResult listTopArticle();

    ResponseResult listRecommendArticle(String articleId, int size);

    ResponseResult listArticlesByLabel(Page page, String label);

    ResponseResult listLabels(int size);

    ResponseResult postNote(Notes notes);

    ResponseResult listNotes();

    ResponseResult deleteNote(String noteId);
}
