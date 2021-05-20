package com.whoami.myblog.dao;

import com.whoami.myblog.entity.FriendLink;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendLinkDao {
    int deleteByPrimaryKey(String id);

    int insert(FriendLink record);

    int insertSelective(FriendLink record);

    FriendLink selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FriendLink record);

    int updateByPrimaryKey(FriendLink record);

    List<FriendLink> selectAllFriendLink();

    List<FriendLink> selectAllBySate(String state);
}