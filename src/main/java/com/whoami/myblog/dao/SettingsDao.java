package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Setting;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsDao {
    int deleteByPrimaryKey(String id);

    int insert(Setting record);

    int insertSelective(Setting setting);

    Setting selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Setting record);

    int updateByPrimaryKey(Setting record);

    Setting selectByKey(String key);
}