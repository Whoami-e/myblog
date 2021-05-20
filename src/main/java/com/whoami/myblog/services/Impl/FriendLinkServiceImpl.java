package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.whoami.myblog.dao.FriendLinkDao;
import com.whoami.myblog.entity.Category;
import com.whoami.myblog.entity.FriendLink;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.FriendLinkService;
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
public class FriendLinkServiceImpl implements FriendLinkService {

    @Autowired
    private FriendLinkDao friendLinkDao;

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private UserService userService;

    @Override
    public ResponseResult addFriendLink(FriendLink friendLink) {
        String url = friendLink.getUrl();
        if (TextUtils.isEmpty(url)) {
            return ResponseResult.FAILED("链接URL不能为空");
        }
        String logo = friendLink.getLogo();
        if (TextUtils.isEmpty(logo)) {
            return ResponseResult.FAILED("LOGO不可以为空");
        }
        String name = friendLink.getName();
        if (TextUtils.isEmpty(name)) {
            return ResponseResult.FAILED("友情链接名称不能为空");
        }

        friendLink.setId(snowFlakeIdWorker.nextId() + "");
        friendLink.setCreateTime(new Date());
        friendLink.setUpdateTime(new Date());

        friendLinkDao.insert(friendLink);
        
        return ResponseResult.SUCCESS("创建成功");
    }

    @Override
    public ResponseResult getFriendLink(String friendLinkId) {
        FriendLink friendLink = friendLinkDao.selectByPrimaryKey(friendLinkId);
        if (friendLink == null) {
            return ResponseResult.FAILED("该友情链接不存在");
        }
        return ResponseResult.SUCCESS("获取友情链接成功").setData(friendLink);
    }

    @Override
    public ResponseResult listFriendLink() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        //判断角色
        List<FriendLink> friendLinks;
        User user = userService.checkUser(request, response);
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            friendLinks = friendLinkDao.selectAllBySate("1");
        } else {
            friendLinks = friendLinkDao.selectAllFriendLink();
        }

        return ResponseResult.SUCCESS("查询成功").setData(friendLinks);
    }

    @Override
    public ResponseResult deleteFriendLink(String friendLinkId) {
        int i = friendLinkDao.deleteByPrimaryKey(friendLinkId);
        if (i == 0) {
            return ResponseResult.FAILED("友情链接删除失败");
        }
        return ResponseResult.SUCCESS("友情链接删除成功");
    }

    @Override
    public ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink) {
        FriendLink friendLink1 = friendLinkDao.selectByPrimaryKey(friendLinkId);
        if (friendLink1 == null) {
            return ResponseResult.FAILED("友情链接不存在");
        }
        friendLink.setId(friendLinkId);
        friendLink.setUpdateTime(new Date());
        friendLinkDao.updateByPrimaryKeySelective(friendLink);
        return ResponseResult.SUCCESS("更新成功");
    }
}
