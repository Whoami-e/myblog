package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Labels;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabelsDao {
    int deleteByPrimaryKey(String id);

    int insert(Labels record);

    int insertSelective(Labels record);

    Labels selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Labels labels);

    int updateByPrimaryKey(Labels record);

    Labels selectByName(String label);

    int updateByLabelName(String label);

    List<Labels> selectAll();
}