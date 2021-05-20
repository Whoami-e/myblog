package com.whoami.myblog.services;

import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface UserService {

    /**
     * 初始化管理员账号
     * @param user
     * @param request
     * @return
     */
    ResponseResult initManagerAccount(User user, HttpServletRequest request);

    void createCaptcha(HttpServletResponse response, String captchaKey) throws Exception;

    ResponseResult sendEmail(String type, String emailAddress, HttpServletRequest request);

    ResponseResult register(User user, String email_code, String captchaCode, String captchaKey, HttpServletRequest request);

    ResponseResult doLogin(User user, String captcha, String captchaKey, String from);

    User checkUser(HttpServletRequest request,HttpServletResponse response);

    ResponseResult getUserInfo(String userId);

    ResponseResult checkEmail(String email);

    ResponseResult checkUserName(String userName);

    ResponseResult updateUserInfo(HttpServletResponse response,HttpServletRequest request,String userId, User user);

    ResponseResult deleteUserById(String userId, HttpServletRequest request, HttpServletResponse response);

    ResponseResult listUsers(Page page, HttpServletRequest request, HttpServletResponse response, String userName, String email);

    ResponseResult updateUserPassword(String emailCode, User user);

    ResponseResult updateEmail(String oldEmail, String newEmail, String verifyCode);

    ResponseResult doLogOut();

    ResponseResult getPcLoginQrCode();

    ResponseResult checkQrCodeLoginState(String loginId);

    ResponseResult updateQrCodeLoginState(String loginId);

    ResponseResult parseToken();

    ResponseResult resetPassword(String userId, String password);

    ResponseResult checkAdmin();

    ResponseResult restoreUserById(String userId, HttpServletRequest request, HttpServletResponse response);
}
