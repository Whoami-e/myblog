package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Images;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagesDao {
    int deleteByPrimaryKey(String id);

    int insert(Images record);

    int insertSelective(Images record);

    Images selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Images record);

    int updateByPrimaryKey(Images record);

    Images selectByMD5(String md5);

    List<Images> selectAll(@Param("userId") String userId, @Param("state") String state, @Param("original") String original);
}