package com.whoami.myblog.controller.admin;

import com.whoami.myblog.entity.Looper;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.LooperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/looper")
public class LooperApi {

    @Autowired
    private LooperService looperService;

    /**
     * 添加轮播图
     * @param looper 轮播图对象
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult addLooper(@RequestBody Looper looper){
        return looperService.addLoop(looper);
    }

    /**
     * 删除轮播图
     * @param looperId 轮播图ID
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{looperId}")
    public ResponseResult deleteLooper(@PathVariable("looperId")String looperId){
        return looperService.deleteLoop(looperId);
    }

    /**
     * 更新轮播图
     * @param looperId 轮播图ID
     * @param looper 轮播图对象
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{looperId}")
    public ResponseResult updateLooper(@PathVariable("looperId")String looperId, @RequestBody Looper looper){
        return looperService.updateLoop(looperId,looper);
    }

    /**
     * 获取单个轮播图信息
     * @param looperId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{looperId}")
    public ResponseResult getLooper(@PathVariable("looperId")String looperId){
        return looperService.getLoop(looperId);
    }

    /**
     * 获取轮播图列表
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listLooper(){
        return looperService.listLoops();
    }
}
