package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Looper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LooperDao {
    int deleteByPrimaryKey(String id);

    int insert(Looper record);

    int insertSelective(Looper record);

    Looper selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Looper record);

    int updateByPrimaryKey(Looper record);

    List<Looper> selectAll();

    List<Looper> selectAllBySate(String state);
}