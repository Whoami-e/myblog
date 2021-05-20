package com.whoami.myblog.services.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.whoami.myblog.dao.RefreshTokenDao;
import com.whoami.myblog.dao.SettingsDao;
import com.whoami.myblog.dao.UserDao;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.entity.RefreshToken;
import com.whoami.myblog.entity.Setting;
import com.whoami.myblog.entity.User;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.UserService;
import com.whoami.myblog.utils.*;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SettingsDao settingsDao;

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TaskService taskService;

    @Autowired
    private Gson gson;

    @Autowired
    private CountDownLatchManager countDownLatchManager;


    @Override
    public ResponseResult initManagerAccount(User user, HttpServletRequest request) {
        //检查是否有初始化
        Setting managerAccountState = settingsDao.selectByKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        if (managerAccountState != null) {
            return ResponseResult.FAILED("管理员账号已经初始化了");
        }
        //检查数据
        if (TextUtils.isEmpty(user.getUserName())) {
            return ResponseResult.FAILED("用户名不能为空！！");
        }
        if (TextUtils.isEmpty(user.getPassword())) {
            return ResponseResult.FAILED("密码不能为空！！");
        }
        if (TextUtils.isEmpty(user.getEmail())) {
            return ResponseResult.FAILED("邮箱不能为空！！");
        }

        //补充数据
        user.setId(String.valueOf(snowFlakeIdWorker.nextId()));
        user.setRoles(Constants.User.ROLE_ADMIN);
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setState(Constants.User.DEFAULT_STATE);
        String remoteAddr = request.getRemoteAddr();
        // System.out.println(remoteAddr);
        user.setLoginIp(remoteAddr);
        user.setRegIp(remoteAddr);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        //对密码加密
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        //保存到数据库
        int i = userDao.insert(user);
        // System.out.println(i);

        //更新已经初始化的标记
        Setting setting = new Setting(String.valueOf(snowFlakeIdWorker.nextId())
                ,Constants.Settings.MANAGER_ACCOUNT_INIT_STATE,"1",new Date(),new Date());
        int insert = settingsDao.insert(setting);
        // System.out.println(insert);
        return ResponseResult.SUCCESS("初始化成功！！");
    }

    //验证码字体数组
    public static final int[] captcha_font_type = {
            Captcha.FONT_1,
            Captcha.FONT_2,
            Captcha.FONT_3,
            Captcha.FONT_4,
            Captcha.FONT_5,
            Captcha.FONT_6,
            Captcha.FONT_7,
            Captcha.FONT_8,
            Captcha.FONT_9,
            Captcha.FONT_10
    };

    //验证码组合类型
    public static final int[] captcha_character_combination_type = {
            Captcha.TYPE_DEFAULT,
            Captcha.TYPE_ONLY_NUMBER,
            Captcha.TYPE_ONLY_CHAR,
            Captcha.TYPE_ONLY_UPPER,
            Captcha.TYPE_ONLY_LOWER,
            Captcha.TYPE_NUM_AND_UPPER
    };

    //生成图灵验证码
    @Override
    public void createCaptcha(HttpServletResponse response, String captchaKey) throws Exception {
        if(TextUtils.isEmpty(captchaKey) || captchaKey.length() < 13){
            return;
        }
        long key;
        try {
            key = Long.parseLong(captchaKey);
        }catch (Exception e) {
            return;
        }

        Random random = new Random();
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //选择验证码类型
        Captcha targetCaptcha = null;
        int captchaType = random.nextInt(2);
        if (captchaType == 0){
            // PNG类型
            targetCaptcha = new SpecCaptcha(120, 40, 5);
        }else if(captchaType == 1){
            //GIF类型
            targetCaptcha = new GifCaptcha(120, 40);
        }

        // 设置字体
        int index1 = random.nextInt(captcha_font_type.length);
        targetCaptcha.setFont(captcha_font_type[index1]);

        // 设置类型，纯数字、纯字母、字母数字混合
        int index2 = random.nextInt(captcha_character_combination_type.length);
        targetCaptcha.setCharType(index2);

        //获取验证码
        String content = targetCaptcha.text().toLowerCase();
        // System.out.println("验证码 == >" + content);

        //保存到Redis中,有效时间5分钟
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT + key,content,60*5);
        //输出图片流
        targetCaptcha.out(response.getOutputStream());
    }

    /**
     * 发送邮件验证码
     * 使用场景：注册，找回密码，修改邮箱
     * 注册(register)：如果当前邮箱注册过，就提示
     * 找回密码(forget)：如果当前邮箱没有注册过，就提示
     * 修改邮箱(update)：新邮箱注册过，就提示
     * @param emailAddress
     * @param request
     * @return
     */
    @Override
    public ResponseResult sendEmail(String type, String emailAddress, HttpServletRequest request) {

        if (emailAddress == null) {
            return ResponseResult.FAILED("邮箱地址不能为空");
        }

        if("register".equals(type) || "update".equals(type)){
            User user = userDao.selectByEmail(emailAddress);
            if (user != null) {
                return ResponseResult.FAILED("该邮箱地址已注册");
            }
        }else if("forget".equals(type)){
            User user = userDao.selectByEmail(emailAddress);
            if (user == null) {
                return ResponseResult.FAILED("该邮箱地址未注册");
            }
        }

        // 防止不断地发送验证码：同一个邮箱，发送间隔要超过60秒，一小时内同一个IP。最多只能发送10次
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null) {
            remoteAddr = remoteAddr.replaceAll(":","_");
        }
        // System.out.println("IP地址 == > " + remoteAddr);
        //从Redis中查询IP地址发送邮件次数
        Integer ipSendTime = null;
        String num = (String)redisUtil.get(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr);
        if (num != null) {
            ipSendTime = Integer.parseInt(num);
        } else {
            ipSendTime = 0;
        }
        if (ipSendTime > 10) {
            return ResponseResult.FAILED("同一个IP地址发送邮件过于频繁，请稍后再试！！");
        }

        //当前邮箱是否发送过消息
        Object send = redisUtil.get(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress);
        // System.out.println("邮箱是否频繁 == >" + send);
        if (send != null) {
            return ResponseResult.FAILED("发送邮件过于频繁，请稍后再试！！");
        }

        // 验证邮箱地址的正确性
        boolean isEmailFormatOk = TextUtils.isEmailFormatOk(emailAddress);
        if (!isEmailFormatOk) {
            return ResponseResult.FAILED("邮箱地址格式不正确！！");
        }
        // 发送验证码
        String str = "1234567890";
        String code = "";
        Random random = new Random();
        //随机生成4位数的验证码
        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(str.length());
            char c = str.charAt(index);
            code += c;
        }
        // System.out.println("随机生成验证码 == >" + code);
        try {
            //异步发送邮件
            taskService.sendEmailCode(code,emailAddress);
        }catch (Exception e){
            return ResponseResult.FAILED("验证码发送失败，请稍后再试！！！");
        }

        //Redis中存入发送记录
        //同一个IP
        if (ipSendTime == null) {
            ipSendTime = 0;
        }
        ipSendTime++;
        //存入Redis中，有效时间为1小时
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr,String.valueOf(ipSendTime),60*60);

        //邮箱限制
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress, "isSend",50);

        //保存邮箱验证码
        redisUtil.set(Constants.User.KEY_EMAIL_CODE_CONTENT + emailAddress,code,60*5);

        return ResponseResult.SUCCESS("邮箱验证码发送成功！！");
    }

    @Override
    public ResponseResult register(User user, String email_code, String captchaCode, String captchaKey, HttpServletRequest request) {

        // 1、检查当前用户名是否注册
        String userName = user.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("用户名不能为空");
        }
        User selectByUserName_User = userDao.selectByUserName(userName);
        if (selectByUserName_User != null) {
            return ResponseResult.FAILED("该用户名已注册");
        }

        // 2、检查邮箱地址
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱地址不能为空");
        }
        if(!TextUtils.isEmailFormatOk(email)){
            return ResponseResult.FAILED("邮箱地址格式不正确");
        }

        User selectByEmail_User = userDao.selectByEmail(email);
        if (selectByEmail_User != null) {
            return ResponseResult.FAILED("该邮箱地址已注册");
        }

        // 3、检查邮箱验证码是否正确
        String emailCode = (String)redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(emailCode)) {
            return ResponseResult.FAILED("邮箱验证码已过期");
        }
        if (!emailCode.equals(email_code)) {
            return ResponseResult.FAILED("邮箱验证码不正确");
        } else {
            redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        }

        // 4、检查图灵验证码是否正确
        String captchaVerifyCode = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (TextUtils.isEmpty(captchaVerifyCode)) {
            return ResponseResult.FAILED("人类验证码已过期");
        }
        if (!captchaVerifyCode.equals(captchaCode)) {
            return ResponseResult.FAILED("人类验证码不正确");
        } else {
            redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        }

        // 5、对密码进行加密
        String password = user.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不能为空");
        }
        user.setPassword(bCryptPasswordEncoder.encode(password));

        // 6、补全剩余数据
        String remoteAddr = request.getRemoteAddr();
        user.setLoginIp(remoteAddr);
        user.setRegIp(remoteAddr);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setRoles(Constants.User.ROLE_NORMAL);
        user.setId(snowFlakeIdWorker.nextId() + "");
        user.setState("1");

        // 7、把用户信息保存到数据库
        userDao.insert(user);

        return ResponseResult.JOIN_IN_SUCCESS();
    }

    @Override
    public ResponseResult doLogin(User user
            , String captcha
            , String captchaKey
            , String from) {

        if (TextUtils.isEmpty(from) || (!Constants.FROM_MOBILE.equals(from) && !Constants.FROM_PC.equals(from))) {
            from = Constants.FROM_MOBILE;
        }

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String captchaCode = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (!captcha.equals(captchaCode)) {
            return ResponseResult.FAILED("人类验证码不正确");
        }
        redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        String userName = user.getUserName();
        // System.out.println("用户名 == >" + userName);
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("用户名不可以为空");
        }
        String password = user.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不可以为空");
        }
        User selectUser = userDao.selectByUserName(userName);

        // System.out.println(selectUser);
        if (selectUser == null) {
            selectUser = userDao.selectByEmail(userName);
        }
        if (selectUser == null) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }

        //用户存在
        boolean matches = bCryptPasswordEncoder.matches(password, selectUser.getPassword());
        if (!matches) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        if (!"1".equals(selectUser.getState())) {
            return ResponseResult.ACCOUNT_BANNED();
        }

        selectUser.setLoginIp(request.getRemoteAddr());
        selectUser.setUpdateTime(new Date());
        //密码正确
        createToken(response, selectUser, from);


        return ResponseResult.LOGIN_SUCCESS();
    }

    /**
     * 生成token以及tokenKey，并把token保存到Redis，tokenKey保存到cookie，最后生成refreshToken
     * @param response
     * @param selectUser
     * @param from
     * @return
     */
    private String createToken(HttpServletResponse response, User selectUser, String from) {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String oldTokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);

        RefreshToken oldRefreshToken = refreshTokenDao.selectByUserId(selectUser.getId());

        if (Constants.FROM_MOBILE.equals(from)) {
            //确保单端登录，删除Redis里的token
            if (oldRefreshToken != null) {
                redisUtil.del(Constants.User.KEY_TOKEN + oldRefreshToken.getMobileTokenKey());
            }
            refreshTokenDao.deleteMobileTokenKey(oldTokenKey);
        } else if (Constants.FROM_PC.equals(from)){
            //确保单端登录，删除Redis里的token
            if (oldRefreshToken != null) {
                redisUtil.del(Constants.User.KEY_TOKEN + oldRefreshToken.getTokenKey());
            }
            refreshTokenDao.deletePCTokenKey(oldTokenKey);
        }

        //生成token以及tokenKey
        Map<String, Object> claims = ClaimsUtils.user2Claims(selectUser,from);
        String token = JwtUtil.createToken(claims);
        String tokenKey = from + DigestUtils.md5DigestAsHex(token.getBytes());

        //保存token到Redis中，有效期为2小时，key是tokenKey
        redisUtil.set(Constants.User.KEY_TOKEN + tokenKey,token,Constants.TimeValue.HOUR_2);

        //把tokenKey保存到cookie里
        CookieUtils.setUpCookie(response,Constants.User.COOKIE_KEY,tokenKey);

        //先判断数据库里有没有RefreshToken，有的话更新，没有的话创建
        boolean isHaveRefreshToken = true;
        RefreshToken refreshToken = refreshTokenDao.selectByUserId(selectUser.getId());
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setId(snowFlakeIdWorker.nextId() + "");
            refreshToken.setCreateTime(new Date());
            refreshToken.setUserId(selectUser.getId());
            isHaveRefreshToken = false;
        }

        //生成refreshToken并保存到数据库
        String refreshTokenValue = JwtUtil.createRefreshToken(selectUser.getId(), Constants.TimeValueMillions.MONTH * 1000);
        refreshToken.setRefreshToken(refreshTokenValue);
        if (Constants.FROM_PC.equals(from)) {
            refreshToken.setTokenKey(tokenKey);
        } else {
            refreshToken.setMobileTokenKey(tokenKey);
        }
        refreshToken.setUpdateTime(new Date());
        if(isHaveRefreshToken){
            refreshTokenDao.updateByPrimaryKeySelective(refreshToken);
        }
        if(!isHaveRefreshToken){
            refreshTokenDao.insert(refreshToken);
        }

        return tokenKey;
    }

    /**
     * 检查用户是否登录状
     * @param request
     * @param response
     * @return 用户信息
     */
    @Override
    public User checkUser(HttpServletRequest request, HttpServletResponse response) {

        //从cookie中获取tokenKey,并根据tokenKey从Redis中获取token
        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return null;
        }

        // System.out.println("checkUser tokenKey ==> " + tokenKey);

        //解析token
        User user = parseByTokenKey(tokenKey);
        //token中要解析次请求时什么端的
        String from = tokenKey.startsWith(Constants.FROM_PC) ? Constants.FROM_PC : Constants.FROM_MOBILE;
        if (user == null) {
            //说明解析出错，token过期了
            // 1、去数据库查询refreshToken
            //如果是从PC，我们以PC的token_key来查，mobile就以mobile_token_key来查
            RefreshToken refreshToken;
            if (Constants.FROM_PC.equals(from)) {
                refreshToken = refreshTokenDao.selectByTokenKey(tokenKey);
            } else {
                refreshToken = refreshTokenDao.selectByMobileTokenKey(tokenKey);
            }
            if (refreshToken == null) {
                System.out.println("checkUser refreshToken ==> NULL");
                return null;
            }
            try {
                //解析refreshToken，创建新的token和refreshToken
                JwtUtil.parseJWT(refreshToken.getRefreshToken());
                User selectByIdUser = userDao.selectByPrimaryKey(refreshToken.getUserId());
                //删除旧的解析refreshToken
                refreshTokenDao.deleteByPrimaryKey(refreshToken.getId());
                String newTokenKey = createToken(response, selectByIdUser, from);
                System.out.println("checkUser create new Token");
                return parseByTokenKey(newTokenKey);
            } catch (Exception e1) {
                System.out.println("checkUser refreshToken ==> 过期");
                return null;
            }
        }
        return user;
    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    @Override
    public ResponseResult getUserInfo(String userId) {
        User user = userDao.selectByPrimaryKey(userId);
        if (user == null) {
            return ResponseResult.FAILED("该用户不存在");
        }
        //复制对象
        String userJson = gson.toJson(user);
        User newUser = gson.fromJson(userJson, User.class);
        newUser.setPassword("");
        newUser.setEmail("");
        newUser.setLoginIp("");
        newUser.setRegIp("");
        return ResponseResult.SUCCESS("获取成功").setData(newUser);
    }

    /**
     * 根据邮箱地址查询
     * @param email
     * @return
     */
    @Override
    public ResponseResult checkEmail(String email) {
        User user = userDao.selectByEmail(email);
        return user == null ? ResponseResult.FAILED("该邮箱未注册") : ResponseResult.SUCCESS("该邮箱已注册");
    }

    /**
     * 根据用户名查询用户
     * @param userName
     * @return
     */
    @Override
    public ResponseResult checkUserName(String userName) {
        User user = userDao.selectByUserName(userName);
        return user == null ? ResponseResult.FAILED("该用户名未注册") : ResponseResult.SUCCESS("该用户名已注册");
    }

    /**
     * 更新用户信息
     * @param response
     * @param request
     * @param userId
     * @param user
     * @return
     */
    @Override
    public ResponseResult updateUserInfo(HttpServletResponse response,HttpServletRequest request,String userId, User user) {
        User checkUser = checkUser(request, response);
        if (checkUser == null) {
            return ResponseResult.NOT_LOGIN();
        }
        User selectByIdUser = userDao.selectByPrimaryKey(checkUser.getId());
        if (!selectByIdUser.getId().equals(userId)) {
            return ResponseResult.PERMISSION_DENIED();
        }
        checkUserName(user.getUserName());
        user.setId(userId);
        user.setUpdateTime(new Date());
        userDao.updateByPrimaryKeySelective(user);
        String cookieKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + cookieKey);
        return ResponseResult.SUCCESS("用户信息更新成功");
    }

    /**
     * 删除用户，并不是真的删除，只是修改账户状态
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @Override
    public ResponseResult deleteUserById(String userId, HttpServletRequest request, HttpServletResponse response) {

        User userFromDb = userDao.selectByPrimaryKey(userId);
        if ("admin".equals(userFromDb.getRoles())) {
            return ResponseResult.FAILED("对管理员无法进行此操作！");
        }
        User user = new User();
        user.setId(userId);
        user.setState("0");
        int i = userDao.updateByPrimaryKeySelective(user);
        if (i > 0){
            return ResponseResult.SUCCESS("删除成功");
        }
        return ResponseResult.FAILED("用户不存在");
    }

    /**
     * 获取用户列表
     * @param page
     * @param request
     * @param response
     * @param userName
     * @param email
     * @return
     */
    @Override
    public ResponseResult listUsers(Page page, HttpServletRequest request, HttpServletResponse response, String userName, String email) {


        Integer pageNum = page.getPageNum();
        Integer pageSize = page.getPageSize();

        PageHelper.startPage(pageNum,pageSize);
        List<User> users = userDao.selectAllUser(userName,email);
        // for (User user : users) {
        //     System.out.println(user.toString());
        // }
        return ResponseResult.SUCCESS("查询成功").setData(new PageInfo<>(users));
    }

    /**
     * 修改密码
     * @param emailCode
     * @param user
     * @return
     */
    @Override
    public ResponseResult updateUserPassword(String emailCode, User user) {

        String emailAddress = user.getEmail();
        if (TextUtils.isEmpty(emailAddress)) {
            return ResponseResult.FAILED("邮箱不能为空");
        }
        String verifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + emailAddress);

        if (verifyCode == null) {
            return ResponseResult.FAILED("邮箱验证码已过期");
        }

        if (!verifyCode.equals(emailCode)) {
            return ResponseResult.FAILED("邮箱验证码错误");
        }
        redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + emailAddress);
        int i = userDao.updateByEmail(bCryptPasswordEncoder.encode(user.getPassword()), emailAddress);

        return i > 0 ? ResponseResult.SUCCESS("密码修改成功") : ResponseResult.FAILED("密码修改失败");
    }

    /**
     * 更新邮箱
     *
     * @param oldEmail
     * @param newEmail
     * @param verifyCode
     * @return
     */
    @Override
    public ResponseResult updateEmail(String oldEmail, String newEmail, String verifyCode) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        User user = checkUser(request, response);
        if (user == null) {
            return ResponseResult.NOT_LOGIN();
        }

        User userFromDb = userDao.selectByPrimaryKey(user.getId());

        if (!oldEmail.equals(userFromDb.getEmail())) {
            return ResponseResult.FAILED("原邮箱地址错误");
        }
        String emailCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + newEmail);
        if (TextUtils.isEmpty(emailCode)) {
            return ResponseResult.FAILED("验证码过期");
        }
        if (!emailCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误");
        }
        redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + newEmail);
        user.setEmail(newEmail);
        String cookieKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + cookieKey);
        int i = userDao.updateById(user.getId(), newEmail);

        return i > 0 ? ResponseResult.SUCCESS("邮箱修改成功") : ResponseResult.FAILED("邮箱修改失败");
    }

    /**
     * 退出登录
     * @return
     */
    @Override
    public ResponseResult doLogOut() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return ResponseResult.NOT_LOGIN();
        }

        //删除Redis里的token
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);

        //删除MySQL里的refreshToken
        if (tokenKey.startsWith(Constants.FROM_PC)) {
            refreshTokenDao.deletePCTokenKey(tokenKey);
        } else {
            refreshTokenDao.deleteMobileTokenKey(tokenKey);
        }

        //删除Cookie里的token_key
        CookieUtils.deleteCookie(response,Constants.User.COOKIE_KEY);

        return ResponseResult.SUCCESS("退出登录成功");
    }

    @Override
    public ResponseResult getPcLoginQrCode() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String lastLoginId = CookieUtils.getCookie(request, Constants.User.LAST_REQUEST_LOGIN_ID);
        if (!TextUtils.isEmpty(lastLoginId)) {
            redisUtil.del(Constants.User.KEY_PC_LOGIN_ID + lastLoginId);
            Object lastGetTime = redisUtil.get(Constants.User.LAST_REQUEST_LOGIN_ID + lastLoginId);
            if (lastGetTime != null) {
                return ResponseResult.FAILED("服务器繁忙，请稍后再试");
            }
        }

        // 1.生成唯一的ID
        long code = snowFlakeIdWorker.nextId();
        // 2.保存到Redis里，值为false，时间为5分钟（二维码的有效时间）
        redisUtil.set(Constants.User.KEY_PC_LOGIN_ID + code,
                Constants.User.KEY_PC_LOGIN_STATE_FALSE,
                Constants.TimeValue.MIN_5);
        // 返回结果
        Map<String,Object> result = new HashMap<>();
        String originalDomain = TextUtils.getDomain(request);
        result.put("code",code);
        result.put("url", originalDomain + "/portal/image/qr-code/" + code);
        CookieUtils.setUpCookie(response,Constants.User.LAST_REQUEST_LOGIN_ID,String.valueOf(code));
        redisUtil.set(Constants.User.LAST_REQUEST_LOGIN_ID + String.valueOf(code), "true", Constants.TimeValue.SECOND_10);

        return ResponseResult.SUCCESS("获取成功").setData(result);
    }

    @Override
    public ResponseResult checkQrCodeLoginState(String loginId) {

        ResponseResult result = checkLoginIdState(loginId);
        if (result != null) return result;
        Callable<ResponseResult> callable = new Callable<ResponseResult>() {
            @Override
            public ResponseResult call() throws Exception {
                try {
                    System.out.println("start waiting for scan ....");
                    countDownLatchManager.getLatch(loginId).await(30, TimeUnit.SECONDS);
                    System.out.println("start check login state ....");
                    ResponseResult checkResult = checkLoginIdState(loginId);
                    if (checkResult != null) return checkResult;
                    return ResponseResult.WAITING_FOR_SCAN();
                } finally {
                    countDownLatchManager.deleteLatch(loginId);
                }
            }
        };
        try {
            return callable.call();
        } catch (Exception e){
            e.printStackTrace();
        }
        return ResponseResult.WAITING_FOR_SCAN();
    }

    @Override
    public ResponseResult updateQrCodeLoginState(String loginId) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        User user = checkUser(request, response);
        if (user == null) {
            return ResponseResult.NOT_LOGIN();
        }
        redisUtil.set(Constants.User.KEY_PC_LOGIN_ID + loginId, user.getId());
        countDownLatchManager.onPhoneDoLogin(loginId);
        return ResponseResult.LOGIN_SUCCESS();
    }

//    @Override
//    public ResponseResult parseToken() {
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = requestAttributes.getRequest();
//        HttpServletResponse response = requestAttributes.getResponse();
//        User user = checkUser(request, response);
//        if (user == null) {
//            return ResponseResult.FAILED("用户未登录");
//        }
//        return ResponseResult.SUCCESS("获取成功").setData(user);
//    }

    @Override
    public ResponseResult resetPassword(String userId, String password) {

        User user = userDao.selectByPrimaryKey(userId);
        if (user == null) {
            return ResponseResult.FAILED("该用户不存在");
        }
        user.setPassword(bCryptPasswordEncoder.encode(password));

        int i = userDao.updateByPrimaryKeySelective(user);
        if (i >0) {
            return ResponseResult.SUCCESS("密码重置成功");
        }
        return ResponseResult.FAILED("密码重置失败");
    }

    @Override
    public ResponseResult checkAdmin() {

        Setting managerAccountState = settingsDao.selectByKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        if (managerAccountState == null) {
            return ResponseResult.FAILED("管理员账号未初始化");
        }

        return ResponseResult.SUCCESS("管理员账号已经初始化了");
    }

    @Override
    public ResponseResult restoreUserById(String userId, HttpServletRequest request, HttpServletResponse response) {
        User userFromDb = userDao.selectByPrimaryKey(userId);
        if ("admin".equals(userFromDb.getRoles())) {
            return ResponseResult.FAILED("对管理员无法进行此操作！");
        }
        User user = new User();
        user.setId(userId);
        user.setState("1");
        int i = userDao.updateByPrimaryKeySelective(user);
        if (i > 0){
            return ResponseResult.SUCCESS("恢复成功");
        }
        return ResponseResult.FAILED("用户不存在");
    }

    private ResponseResult checkLoginIdState(String loginId) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        String loginSate = (String) redisUtil.get(Constants.User.KEY_PC_LOGIN_ID + loginId);
        if (loginSate == null) {
            //二维码过期
            return ResponseResult.QR_CODE_DEPRECATE();
        }
        if (!TextUtils.isEmpty(loginSate) && !Constants.User.KEY_PC_LOGIN_STATE_FALSE.equals(loginSate)) {
            User user = userDao.selectByPrimaryKey(loginSate);
            if (user == null) {
                return ResponseResult.QR_CODE_DEPRECATE();
            }
            String token = createToken(response, user, Constants.FROM_PC);
            return ResponseResult.LOGIN_SUCCESS();
        }

        return null;
    }

    /**
     * 解析token
     * @param tokenKey
     * @return
     */
    private User parseByTokenKey(String tokenKey){
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        if (token != null) {
            try {
                Claims claims = JwtUtil.parseJWT(token);
                // System.out.println("parseByTokenKey claims ==> " + claims.toString());
                User user = ClaimsUtils.claims2User(claims);
                return user;
            } catch (Exception e){
                // System.out.println("parseByTokenKey ==>" + token + "过期了");
                return null;
            }
        }
        return null;
    }

    /**
     * 解析token是PC端来的还是移动端来的
     * @param tokenKey
     * @return
     */
    private String parseFrom(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        if (token != null) {
            try {
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.getFrom(claims);
            } catch (Exception e) {
                // System.out.println("parseByTokenKey ==>" + token + "过期了");
                return null;
            }

        }
        return null;
    }
    
    @Override
    public ResponseResult parseToken() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        User user = checkUser(request, response);
        if (user == null) {
            return ResponseResult.FAILED("用户未登录");
        }
        return ResponseResult.SUCCESS("获取成功").setData(user);
    }
}
