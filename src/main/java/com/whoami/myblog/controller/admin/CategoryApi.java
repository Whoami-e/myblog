package com.whoami.myblog.controller.admin;

import com.whoami.myblog.entity.Category;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

/**
 * 分类
 */
@RestController
@RequestMapping("/admin/category")
public class CategoryApi {

    @Autowired
    private CategoryService categoryService;

    /**
     * 添加 分类
     * @param category
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult addCategory(@RequestBody Category category){
        return categoryService.addCategory(category);
    }

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{categoryId}")
    public ResponseResult deleteCategory(@PathVariable("categoryId") String categoryId){
        return categoryService.deleteCategory(categoryId);
    }

    /**
     * 恢复分类
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/restore/{categoryId}")
    public ResponseResult restoreCategory(@PathVariable("categoryId") String categoryId){
        return categoryService.restoreCategory(categoryId);
    }

    /**
     * 更新分类
     * @param categoryId
     * @param category
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{categoryId}")
    public ResponseResult updateCategory(@PathVariable("categoryId") String categoryId, @RequestBody Category category){
        return categoryService.updateCategory(categoryId,category);
    }

    /**
     * 获取分类
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{categoryId}")
    public ResponseResult getCategory(@PathVariable("categoryId") String categoryId){
        return categoryService.getCategory(categoryId);
    }

    /**
     * 获取全部分类
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listCategory(){
        return categoryService.listCategories();
    }
}
