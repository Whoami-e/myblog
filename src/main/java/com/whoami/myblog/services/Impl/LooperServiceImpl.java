package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.whoami.myblog.dao.LooperDao;
import com.whoami.myblog.entity.FriendLink;
import com.whoami.myblog.entity.Looper;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.LooperService;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.SnowFlakeIdWorker;
import com.whoami.myblog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LooperServiceImpl implements LooperService {

    @Autowired
    private LooperDao looperDao;

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private UserService userService;

    @Override
    public ResponseResult addLoop(Looper looper) {
        String title = looper.getTitle();
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("标题不可以为空");
        }
        String imageUrl = looper.getImageUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            return ResponseResult.FAILED("图片不可以为空");
        }
        String targetUrl = looper.getTargetUrl();
        if (TextUtils.isEmpty(targetUrl)) {
            return ResponseResult.FAILED("跳转链接不可以为空");
        }
        looper.setId(snowFlakeIdWorker.nextId() + "");
        looper.setCreateTime(new Date());
        looper.setUpdateTime(new Date());

        looperDao.insert(looper);

        return ResponseResult.SUCCESS("轮播图添加成功");
    }

    @Override
    public ResponseResult getLoop(String looperId) {
        Looper looper = looperDao.selectByPrimaryKey(looperId);
        if (looper == null) {
            return ResponseResult.FAILED("轮播图不存在");
        }
        return ResponseResult.SUCCESS("获取轮播图成功").setData(looper);
    }

    @Override
    public ResponseResult listLoops() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        //判断角色
        List<Looper> looperList;
        User user = userService.checkUser(request, response);
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            looperList = looperDao.selectAllBySate("1");
        } else {
            looperList = looperDao.selectAll();
        }

        return ResponseResult.SUCCESS("获取轮播图列表成功").setData(looperList);
    }

    @Override
    public ResponseResult updateLoop(String looperId, Looper looper) {
        Looper looper1 = looperDao.selectByPrimaryKey(looperId);
        if (looper1 == null) {
            return ResponseResult.FAILED("轮播图不存在");
        }
        looper.setId(looperId);
        looper.setUpdateTime(new Date());
        looperDao.updateByPrimaryKeySelective(looper);
        return ResponseResult.SUCCESS("轮播图更新成功");
    }

    @Override
    public ResponseResult deleteLoop(String looperId) {
        int i = looperDao.deleteByPrimaryKey(looperId);
        if (i > 0) {
            return ResponseResult.SUCCESS("轮播图删除成功");
        }
        return ResponseResult.FAILED("轮播图删除失败");
    }
}
