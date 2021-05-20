package com.whoami.myblog.controller.admin;

import com.whoami.myblog.entity.FriendLink;
import com.whoami.myblog.entity.Page;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.FriendLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/friend_link")
public class FriendLinkApi {

    @Autowired
    private FriendLinkService friendLinkService;

    /**
     * 添加分类
     * @param friendLink 友链对象
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult addFriendLink(@RequestBody FriendLink friendLink){
        return friendLinkService.addFriendLink(friendLink);
    }

    /**
     * 删除友链
     * @param friendLinkId 友链ID
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{friendLinkId}")
    public ResponseResult deleteFriendLink(@PathVariable("friendLinkId")String friendLinkId){
        return friendLinkService.deleteFriendLink(friendLinkId);
    }

    /**
     * 更新友链
     * @param friendLinkId
     * @param friendLink
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{friendLinkId}")
    public ResponseResult updateFriendLink(@PathVariable("friendLinkId")String friendLinkId,@RequestBody FriendLink friendLink){
        return friendLinkService.updateFriendLink(friendLinkId,friendLink);
    }

    /**
     * 获取单个友链信息
     * @param friendLinkId 友链ID
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{friendLinkId}")
    public ResponseResult getFriendLink(@PathVariable("friendLinkId")String friendLinkId){
        return friendLinkService.getFriendLink(friendLinkId);
    }

    /**
     * 获取友链列表
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listFriendLink(){
        return friendLinkService.listFriendLink();
    }
}
