package com.whoami.myblog.services.Impl;

import com.whoami.myblog.entity.User;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.CookieUtils;
import com.whoami.myblog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//权限管理
@Service("permission")
public class PermissionService {

    @Autowired
    private UserService userService;

    /**
     * 判断是不是管路员
     * @return
     */
    public boolean admin(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return false;
        }

        User user = userService.checkUser(request, response);
        if (user == null) {
            return false;
        }
        if (Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            //管理员
            return true;
        }
        return false;
    }
}
