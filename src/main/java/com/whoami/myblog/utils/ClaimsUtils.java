package com.whoami.myblog.utils;

import com.whoami.myblog.entity.User;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.Map;

/**
 * token的数据生成和解码工具类
 */
public class ClaimsUtils {

    public static final String ID = "id";
    public static final String USER_NAME = "user_name";
    public static final String ROLES = "roles";
    public static final String AVATAR = "avatar";
    public static final String EMAIL = "email";
    public static final String SIGN = "sign";
    public static final String FROM = "from";

    public static Map<String,Object> user2Claims(User user, String from){

        Map<String,Object> claims = new HashMap<>();
        claims.put(ID,user.getId());
        claims.put(USER_NAME,user.getUserName());
        claims.put(ROLES,user.getRoles());
        claims.put(AVATAR,user.getAvatar());
        claims.put(EMAIL,user.getEmail());
        claims.put(SIGN,user.getSign());
        claims.put(FROM,from);

        return claims;
    }

    public static User claims2User(Claims claims){

        User user = new User();

        String id = (String) claims.get(ID);
        user.setId(id);

        String userName = (String) claims.get(USER_NAME);
        user.setUserName(userName);

        String roles = (String) claims.get(ROLES);
        user.setRoles(roles);

        String avatar = (String) claims.get(AVATAR);
        user.setAvatar(avatar);

        String email = (String) claims.get(EMAIL);
        user.setEmail(email);

        String sign = (String) claims.get(SIGN);
        user.setSign(sign);

        return user;
    }

    public static String getFrom(Claims claims) {
        return (String) claims.get(FROM);
    }
}
