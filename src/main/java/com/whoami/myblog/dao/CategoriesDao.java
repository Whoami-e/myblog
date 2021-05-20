package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesDao {
    int deleteByPrimaryKey(String id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    List<Category> selectAllCategories();

    List<Category> selectAllBySate(String state);
}