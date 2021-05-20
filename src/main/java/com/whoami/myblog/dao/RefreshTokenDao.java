package com.whoami.myblog.dao;

import com.whoami.myblog.entity.RefreshToken;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenDao {
    int deleteByPrimaryKey(String id);

    int insert(RefreshToken refreshToken);

    int insertSelective(RefreshToken refreshToken);

    RefreshToken selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RefreshToken refreshToken);

    int updateByPrimaryKey(RefreshToken refreshToken);

    RefreshToken selectByTokenKey(String tokenKey);

    RefreshToken selectByMobileTokenKey(String mobileTokenKey);

    int deleteByUserId(String userId);

    int deleteByTokenKey(String tokenKey);

    int deleteMobileTokenKey(String tokenKey);

    int deletePCTokenKey(String tokenKey);

    RefreshToken selectByUserId(String userId);
}