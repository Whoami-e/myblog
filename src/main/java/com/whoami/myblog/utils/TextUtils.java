package com.whoami.myblog.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    public static boolean isEmpty(String text){
        return text == null || text.length() == 0;
    }

    //验证邮箱地址的正确性
    public static boolean isEmailFormatOk(String emailAddress){
        String regEx = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(emailAddress);
        return m.matches();
    }

    public static String getDomain(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        StringBuffer requestURL = request.getRequestURL();
        String originalDomain = requestURL.toString().replace(servletPath,"");
        return originalDomain;
    }
}
