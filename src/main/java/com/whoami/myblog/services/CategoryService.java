package com.whoami.myblog.services;

import com.whoami.myblog.entity.Category;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface CategoryService {
    ResponseResult addCategory(Category category);

    ResponseResult getCategory(String categoryId);

    ResponseResult listCategories();

    ResponseResult updateCategory(String categoryId, Category category);

    ResponseResult deleteCategory(String categoryId);

    ResponseResult restoreCategory(String categoryId);
}
