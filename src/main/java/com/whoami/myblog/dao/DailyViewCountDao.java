package com.whoami.myblog.dao;

import com.whoami.myblog.entity.DailyViewCount;

public interface DailyViewCountDao {
    int deleteByPrimaryKey(String id);

    int insert(DailyViewCount record);

    int insertSelective(DailyViewCount record);

    DailyViewCount selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(DailyViewCount record);

    int updateByPrimaryKey(DailyViewCount record);
}