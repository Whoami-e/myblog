package com.whoami.myblog;

import com.whoami.myblog.dao.UserDao;
import com.whoami.myblog.services.SolrService;
import com.whoami.myblog.utils.EmailSender;
import com.whoami.myblog.utils.JwtUtil;
import com.whoami.myblog.utils.RedisUtil;
import com.whoami.myblog.utils.SnowFlakeIdWorker;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class MyBlogApplicationTests {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SnowFlakeIdWorker snowFlakeIdWorker;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void  password() {
        System.out.println(bCryptPasswordEncoder.encode("91b9419d27d4597d7f007eede862b015"));
    }

    @Test
    void contextLoads() throws MessagingException {
        EmailSender.subject("测试邮件发送")
                .from("博客")
                .text("这是发送的内容：ad3bn")
                .to("Enirilee@163.com")
                .send();
    }

    @Test
    void redisSet(){
        redisUtil.incr("age",1);
    }

    @Test
    void redisGet(){
        System.out.println((String) redisUtil.get("name"));
    }

    @Test
    void selectByUserName(){
        System.out.println(userDao.selectByUserName("张七").toString());
    }

    @Test
    void createToken(){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "722250648279580673");
        claims.put("userName", "测试用户");
        claims.put("role", "role_normal");
        claims.put("avatar", "https://cdn.sunofbeaches.com/images/default_avatar.png");
        claims.put("email", "test@sunofbeach.net");
        String token = JwtUtil.createToken(claims);//有效期为1分
        System.out.println(token);
    }

    @Test
    void paresToken(){
        Claims claims = JwtUtil.parseJWT("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiLlvKDkuIkiLCJyb2xlcyI6ImFkbWluIiwic2lnbiI6IuetvuWQjSIsImZyb20iOiJwXyIsImlkIjoiODAyODUxNjUyODM4MDk2ODk2IiwiYXZhdGFyIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3BvcnRhbC9pbWFnZS8xNjE0MTY4MTA0OTczXzgxNDIyNTU3NjMyMjY2MjQwMC5wbmciLCJleHAiOjE2MTQxNzUzMTAsImVtYWlsIjoiRW5pcmlsZWVAMTYzLmNvbSJ9.vEkqvbSsu9POHpXCT9DvrEoPa0bDVko6qvohS2pvB_g");
        //==============================================//
        Object id = claims.get("id");
        Object name = claims.get("user_name");
        Object role = claims.get("roles");
        Object avatar = claims.get("avatar");
        Object email = claims.get("email");

        System.out.println("id == > " + id);
        System.out.println("name == > " + name);
        System.out.println("role == > " + role);
        System.out.println("avatar == > " + avatar);
        System.out.println("email == > " + email);
    }

    @Test
    void test(){
        String s = "806198061192708096.jpg";
        String[] split = s.split("\\.");
        for (String s1 : split) {
            System.out.println(s1);
        }
    }
    @Autowired
    private SolrService solrService;
    @Test
    void delete(){
        solrService.deleteArticle("810525793414283264");
    }


    @Test
    void IP(){
        PrintWriter out;
        out= DriverManager.getLogWriter();
        if(out!=null){
            out.println("得到信息");
            out.close();
        }
    }
}
