package com.whoami.myblog.services;

import com.whoami.myblog.entity.Looper;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;

public interface LooperService {
    ResponseResult addLoop(Looper looper);

    ResponseResult getLoop(String looperId);

    ResponseResult listLoops();

    ResponseResult updateLoop(String looperId, Looper looper);

    ResponseResult deleteLoop(String looperId);
}
