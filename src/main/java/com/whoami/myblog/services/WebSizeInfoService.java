package com.whoami.myblog.services;

import com.whoami.myblog.response.ResponseResult;

public interface WebSizeInfoService {
    ResponseResult getWebSizeTitle();

    ResponseResult putWebSizeTitle(String title);

    ResponseResult getSeoInfo();

    ResponseResult putSeoInfo(String keywords, String description);

    ResponseResult getWebSizeViewCount();

    void updateViewCount();

}
