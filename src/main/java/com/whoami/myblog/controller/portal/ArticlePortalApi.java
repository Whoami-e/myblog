package com.whoami.myblog.controller.portal;

import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ArticleService;
import com.whoami.myblog.services.CategoryService;
import com.whoami.myblog.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/article")
public class ArticlePortalApi {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    /**
     *获取文章列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/list/{pageNum}/{pageSize}")
    public ResponseResult listArticle(@PathVariable("pageNum") int pageNum,@PathVariable("pageSize") int pageSize){
        Page page = new Page(pageNum,pageSize);
        return articleService.listArticles(page,"","", Constants.Article.STATE_PUBLISH);
    }

    /**
     * 根据分类获取文章
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/list/{categoryId}/{pageNum}/{pageSize}")
    public ResponseResult listArticleByCategoryId(@PathVariable("categoryId") String categoryId,@PathVariable("pageNum") int pageNum,@PathVariable("pageSize") int pageSize){
        Page page = new Page(pageNum,pageSize);
        return articleService.listArticles(page,"",categoryId,Constants.Article.STATE_PUBLISH);
    }

    /**
     * 获取文章详情
     * @param articleId
     * @return
     */
    @GetMapping("/{articleId}")
    public ResponseResult getArticleDetail(@PathVariable("articleId") String articleId){
        return articleService.getArticle(articleId);
    }

    /**
     * 获取推荐文章，根据文章的标签随机获取
     * @param articleId
     * @return
     */
    @GetMapping("/recommend/{articleId}/{size}")
    public ResponseResult getRecommendArticle(@PathVariable("articleId") String articleId, @PathVariable("size") int size){
        return articleService.listRecommendArticle(articleId,size);
    }

    /**
     * 获取置顶文章
     * @return
     */
    @GetMapping("/top")
    public ResponseResult getTopArticle(){
        return articleService.listTopArticle();
    }

    /**
     * 获取标签云
     * @param size
     * @return
     */
    @GetMapping("/label/{size}")
    public ResponseResult getLabels(@PathVariable("size") int size){
        return articleService.listLabels(size);
    }

    /**
     * 根据标签获取文章列表
     * @param pageNum
     * @param pageSize
     * @param label
     * @return
     */
    @GetMapping("/list/label/{label}/{pageNum}/{pageSize}")
    public ResponseResult listArticleByLabel(@PathVariable("pageNum") int pageNum,@PathVariable("pageSize") int pageSize, @PathVariable("label") String label){
        Page page = new Page(pageNum,pageSize);
        return articleService.listArticlesByLabel(page,label);
    }

    /**
     * 获取分类
     * @return
     */
    @GetMapping("/categories")
    public ResponseResult getCategories(){
        return categoryService.listCategories();
    }
}
