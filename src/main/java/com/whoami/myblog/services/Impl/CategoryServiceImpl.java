package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.whoami.myblog.dao.CategoriesDao;
import com.whoami.myblog.entity.Category;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.CategoryService;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.SnowFlakeIdWorker;
import com.whoami.myblog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private CategoriesDao categoriesDao;

    @Autowired
    private UserService userService;

    /**
     * 添加分类
     * @param category
     * @return
     */
    @Override
    public ResponseResult addCategory(Category category) {

        if (TextUtils.isEmpty(category.getName())) {
            return ResponseResult.FAILED("分类名称不可以为空");
        }
        if (TextUtils.isEmpty(category.getPinyin())) {
            return ResponseResult.FAILED("分类名称拼音不可以为空");
        }
        if (TextUtils.isEmpty(category.getDescription())) {
            return ResponseResult.FAILED("分类描述不可以为空");
        }
        //补全数据
        category.setId(snowFlakeIdWorker.nextId() + "");
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());

        categoriesDao.insert(category);

        return ResponseResult.SUCCESS("添加分类成功");
    }

    /**
     * 获取分类
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult getCategory(String categoryId) {
        Category category = categoriesDao.selectByPrimaryKey(categoryId);
        if (category == null) {
            return ResponseResult.FAILED("分类不存在");
        }
        return ResponseResult.SUCCESS("获取分类成功").setData(category);
    }

    /**
     * 获取分类列表
     * @return
     */
    @Override
    public ResponseResult listCategories() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        //判断角色
        List<Category> categoryList;
        User user = userService.checkUser(request, response);
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            categoryList = categoriesDao.selectAllBySate("1");
        } else {
            categoryList = categoriesDao.selectAllCategories();
        }

        return ResponseResult.SUCCESS("查询成功").setData(categoryList);
    }

    /**
     * 更新分类
     * @param categoryId
     * @param category
     * @return
     */
    @Override
    public ResponseResult updateCategory(String categoryId, Category category) {
        Category selectByIdCategory = categoriesDao.selectByPrimaryKey(categoryId);
        if (selectByIdCategory == null) {
            return ResponseResult.FAILED("分类不存在");
        }

        category.setId(categoryId);
        category.setUpdateTime(new Date());

        categoriesDao.updateByPrimaryKeySelective(category);

        return ResponseResult.SUCCESS("更新成功");
    }

    /**
     * 删除分类，但是不是正在意义上的删除，只是把状态改成禁用
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult deleteCategory(String categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        category.setStatus("0");

        int i = categoriesDao.updateByPrimaryKeySelective(category);

        if (i == 0) {
            return ResponseResult.FAILED("该分类不存在");
        }
        return ResponseResult.SUCCESS("删除分类成功");
    }

    @Override
    public ResponseResult restoreCategory(String categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        category.setStatus("1");

        int i = categoriesDao.updateByPrimaryKeySelective(category);

        if (i == 0) {
            return ResponseResult.FAILED("该分类不存在");
        }
        return ResponseResult.SUCCESS("恢复分类成功");
    }
}
