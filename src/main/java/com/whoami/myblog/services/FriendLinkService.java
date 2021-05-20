package com.whoami.myblog.services;

import com.whoami.myblog.entity.FriendLink;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;

public interface FriendLinkService {
    ResponseResult addFriendLink(FriendLink friendLink);

    ResponseResult getFriendLink(String friendLinkId);

    ResponseResult listFriendLink();

    ResponseResult deleteFriendLink(String friendLinkId);

    ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink);
}
