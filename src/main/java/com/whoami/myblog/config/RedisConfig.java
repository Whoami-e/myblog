package com.whoami.myblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

}
//@Configuration
//public class RedisConfig {
//
//    @Bean//参数--一个工厂
//    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate redisTemplate = new RedisTemplate();
//        //给redis模板先设置连接工厂，在设置序列化规则
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        //设置序列化规则
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer());
//        redisTemplate.setHashKeySerializer(genericJackson2JsonRedisSerializer());
//        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer());
//        return redisTemplate;
//    }
//
//    @Bean
//    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(){
//        return new GenericJackson2JsonRedisSerializer();
//    }
//}
