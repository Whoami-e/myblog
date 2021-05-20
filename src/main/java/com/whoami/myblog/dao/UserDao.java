package com.whoami.myblog.dao;

import com.whoami.myblog.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao {
    int deleteByPrimaryKey(String id);

    int insert(User user);

    int insertSelective(User record);

    User selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(User user);

    int updateByPrimaryKey(User user);

    User selectByUserName(String userName);

    User selectByEmail(String email);

    List<User> selectAllUser(@Param("userName") String userName,@Param("email") String email);

    int updateByEmail(@Param("password") String password, @Param("email") String emailAddress);

    int updateById(@Param("userId") String id, @Param("email")String email);
}