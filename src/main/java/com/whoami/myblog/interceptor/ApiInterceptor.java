package com.whoami.myblog.interceptor;

import com.google.gson.Gson;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.utils.Constants;
import com.whoami.myblog.utils.CookieUtils;
import com.whoami.myblog.utils.RedisUtil;
import com.whoami.myblog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * 拦截提交过于频繁
 */
@Component
public class ApiInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Gson gson;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            CheckTooFrequentCommit methodAnnotation = handlerMethod.getMethodAnnotation(CheckTooFrequentCommit.class);
            if (methodAnnotation != null) {
                String methodName = handlerMethod.getMethod().getName();
                //所有提交内容的方法，必须用户登录的，所以使用token作为key来记录请求频率
                String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_KEY);
                // System.out.println("tokenKey ==> "+tokenKey);
                if (!TextUtils.isEmpty(tokenKey)) {
                    String hasCommit = (String) redisUtil.get(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey + "_" + methodName);
                    if (!TextUtils.isEmpty(hasCommit)) {
                        //从redis里获取，判断是否存在，如果存在，则返回提交太频繁
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        ResponseResult failed = ResponseResult.FAILED("提交过于频繁,请稍后重试.");
                        PrintWriter writer = response.getWriter();
                        writer.write(gson.toJson(failed));
                        writer.flush();
                        return false;
                    } else {
                        //如果不存在，说明可以提交，并且记录此次提交，有效期为30秒
                        redisUtil.set(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey + "_" + methodName, "true", Constants.TimeValue.SECOND_10);
                    }
                }
                //去判断是否真提交太频繁了
                System.out.println("check commit too frequent ...");
            }
        }
        //true表示放行,false表示拦截
        return true;
    }
}
