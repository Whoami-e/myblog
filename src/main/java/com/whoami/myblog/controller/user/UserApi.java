package com.whoami.myblog.controller.user;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.RedisUtil;
import com.whoami.myblog.utils.SnowFlakeIdWorker;
import com.whoami.myblog.utils.TextUtils;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 初始化管理员账号
     * @return
     */
    @PostMapping("/admin_account")
    public ResponseResult initManagerAccount(@RequestBody User user, HttpServletRequest request){
        // System.out.println(user.getUserName());
        // System.out.println(user.getPassword());
        // System.out.println(user.getEmail());
        return userService.initManagerAccount(user,request);
    }

    @GetMapping("/check_admin")
    public ResponseResult checkAdmin(){
        return userService.checkAdmin();
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("/join_in")
    public ResponseResult register(@RequestBody User user
            ,@RequestParam("email_code")String email_code
            ,@RequestParam("captcha_code")String captchaCode
            ,@RequestParam("captcha_key")String captchaKey
            ,HttpServletRequest request){
        return userService.register(user,email_code,captchaCode,captchaKey,request);
    }

    /**
     * 登录
     * @param captcha
     * @param user
     * @return
     */
    @PostMapping("/login/{captcha}/{captcha_key}")
    public ResponseResult login(@PathVariable("captcha_key") String captchaKey
            ,@PathVariable("captcha") String captcha
            ,@RequestBody User user
            ,@RequestParam(value = "from",required = false)String from){
        return userService.doLogin(user,captcha,captchaKey,from);
    }


    /**
     * 生成图灵验证码
     * @return
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, @RequestParam("captcha_key")String captchaKey) throws Exception {
        try {
            userService.createCaptcha(response,captchaKey);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 发送邮件
     * 使用场景：注册，找回密码，修改邮箱
     * 注册：如果当前邮箱注册过，就提示 type = register
     * 找回密码：如果当前邮箱没有注册过，就提示 type = forget
     * 修改邮箱：新邮箱注册过，就提示 type = update
     * @param emailAddress
     * @return
     */
    @GetMapping("/email_code")
    public ResponseResult sendVerifyCode(HttpServletRequest request,@RequestParam("email") String emailAddress,@RequestParam("type") String type){
        // System.out.println("email == >" + emailAddress);
        return userService.sendEmail(type,emailAddress,request);
    }

    /**
     * 修改密码
     * @param user
     * @return
     */
    @PutMapping("/password/{emailCode}")
    public ResponseResult updatePassword(@PathVariable("emailCode")String emailCode,@RequestBody User user){
        return userService.updateUserPassword(emailCode,user);
    }

    /**
     * 获取作者信息
     * @param userId
     * @return
     */
    @GetMapping("/user_info/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId){
        return userService.getUserInfo(userId);
    }

    /**
     * 修改用户信息
     *
     * 允许用户修改的内容
     * 1、头像
     * 2、用户名（唯一）
     * 3、签名
     * 4、密码
     * 5、邮箱（唯一）
     *
     * @param user
     * @return
     */
    @PutMapping("/user_info/{userId}")
    public ResponseResult updateUserInfo(HttpServletResponse response
            ,HttpServletRequest request
            ,@PathVariable("userId") String userId
            ,@RequestBody User user){
        return userService.updateUserInfo(response,request,userId,user);
    }

    /**
     * 获取用户列表
     * 需要管理员权限
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listUsers(@RequestParam("page") int page
            ,@RequestParam("size") int size
            ,@RequestParam(value = "userName",required = false) String userName
            ,@RequestParam(value = "email",required = false) String email
            ,HttpServletRequest request
            ,HttpServletResponse response){
        Page page1 = new Page(page,size);
        return userService.listUsers(page1,request,response,userName,email);
    }

    /**
     * 删除用户，需要管理员权限
     * @param userId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(HttpServletRequest request, HttpServletResponse response, @PathVariable("userId") String userId){
        return userService.deleteUserById(userId,request,response);
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/{userId}")
    public ResponseResult restoreUser(HttpServletRequest request, HttpServletResponse response, @PathVariable("userId") String userId){
        return userService.restoreUserById(userId,request,response);
    }

    /**
     * 检查邮箱是否唯一
     * @param email
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "表示当前邮箱已注册"),
            @ApiResponse(code = 40000, message = "表示当前邮箱未注册")
    })
    @GetMapping("/email")
    public ResponseResult checkEmail(@RequestParam("email")String email){
        return userService.checkEmail(email);
    }

    /**
     * 检查用户名是否唯一
     * @param userName
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "表示当前用户名已注册"),
            @ApiResponse(code = 40000, message = "表示当前用户名未注册")
    })
    @GetMapping("/user_name")
    public ResponseResult checkUserName(@RequestParam("userName")String userName){
        return userService.checkUserName(userName);
    }

    /**
     * 更新邮箱
     * @param oldEmail
     * @param newEmail
     * @param verifyCode
     * @return
     */
    @PutMapping("/email")
    public ResponseResult updateEmail(@RequestParam("oldEmail")String oldEmail, @RequestParam("newEmail")String newEmail, @RequestParam("verify_code")String verifyCode){
        return userService.updateEmail(oldEmail,newEmail,verifyCode);
    }

    /**
     * 退出登录
     * @return
     */
    @GetMapping("/logout")
    public ResponseResult logout(){
        return userService.doLogOut();
    }

    /**
     * 获取二维码
     * @return
     */
    @GetMapping("/pc-login-qr-code")
    public ResponseResult getPcLoginQrCode(){
        // 1.生成唯一的ID
        // 2.保存到Redis里，值为false，时间为5分钟（二维码的有效时间）
        // 返回结果
        return userService.getPcLoginQrCode();
    }

    @GetMapping("/qr-code-state/{loginId}")
    public ResponseResult checkQrCodeLoginState(@PathVariable("loginId")String loginId){
        return userService.checkQrCodeLoginState(loginId);
    }

    @PutMapping("/qr-code-state/{loginId}")
    public ResponseResult updateQrCodeLoginState(@PathVariable("loginId")String loginId){
        return userService.updateQrCodeLoginState(loginId);
    }

    //验证token
    @GetMapping("/check-token")
    public ResponseResult parseToken(){
        return userService.parseToken();
    }

    /**
     * 管理重置普通用户密码
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/reset-password/{userId}")
    public ResponseResult resetPassword(@PathVariable("userId")String userId,@RequestParam("password")String password){
        return userService.resetPassword(userId,password);
    }
}
