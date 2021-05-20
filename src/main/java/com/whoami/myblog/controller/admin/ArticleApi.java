package com.whoami.myblog.controller.admin;

import com.whoami.myblog.entity.Article;
import com.whoami.myblog.entity.Notes;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/article")
public class ArticleApi {

    @Autowired
    private ArticleService articleService;

    /**
     * @CheckTooFrequentCommit 检查频繁提交
     * @param article
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult postArticle(@RequestBody Article article){
        return articleService.postArticle(article);
    }

    /**
     * 提交速记
     * @param notes
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping("/note")
    public ResponseResult postNote(@RequestBody Notes notes){
        return articleService.postNote(notes);
    }

    /**
     * 真删
     * @param articleId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{articleId}")
    public ResponseResult deleteArticle(@PathVariable("articleId")String articleId){
        return articleService.deleteArticle(articleId);
    }

    /**
     * 更新文章
     * @param articleId 文章ID
     * @param article 文章对象
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{articleId}")
    public ResponseResult updateArticle(@PathVariable("articleId")String articleId, @RequestBody Article article){
        return articleService.updateArticle(article,articleId);
    }

    /**
     * 获取文章详细内容
     * @param articleId 文章ID
     * @return 文章对象
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{articleId}")
    public ResponseResult getArticle(@PathVariable("articleId")String articleId){
        return articleService.getArticleForAdmin(articleId);
    }

    /**
     *  获取文章列表
     * @param pageNum 页码
     * @param pageSize 页容量
     * @param keyword 文章标题
     * @param categoryId 分类ID
     * @param state 文章状态
     * @return 文章列表
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{pageNum}/{pageSize}")
    public ResponseResult listArticles(@PathVariable("pageNum") int pageNum,
                                      @PathVariable("pageSize") int pageSize,
                                      @RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "categoryId", required = false) String categoryId,
                                       @RequestParam(value = "state", required = false) String state){
        Page page = new Page(pageNum,pageSize);
        return articleService.listArticles(page,keyword,categoryId,state);
    }

    /**
     * 获取速记列表
     * @return 速记列表
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/note/list")
    public ResponseResult listNotes() {
        return articleService.listNotes();
    }

    /**
     * 删除速记
     * @param noteId 速记ID
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/note/list/{noteId}")
    public ResponseResult deleteNote(@PathVariable("noteId")String noteId){
        return articleService.deleteNote(noteId);
    }

    /**
     * 这方法不是真的删除文章，只是把状态改为删除状态
     * @param articleId 文章ID
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/state/{articleId}")
    public ResponseResult deleteArticleByUpdateState(@PathVariable("articleId")String articleId){
        return articleService.deleteArticleByState(articleId);
    }

    /**
     * 置顶文章
     * @param articleId 文章ID
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/top/{articleId}")
    public ResponseResult topArticle(@PathVariable("articleId")String articleId){
        return articleService.topArticle(articleId);
    }
}
