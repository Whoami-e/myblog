package com.whoami.myblog.controller.portal;

import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.CategoryService;
import com.whoami.myblog.services.FriendLinkService;
import com.whoami.myblog.services.LooperService;
import com.whoami.myblog.services.WebSizeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/web_size_info")
public class WebSizeInfoPortalApi {



    @Autowired
    private FriendLinkService friendLinkService;

    @Autowired
    private LooperService looperService;

    @Autowired
    private WebSizeInfoService webSizeInfoService;



    /**
     * 获取网站title
     * @return
     */
    @GetMapping("/title")
    public ResponseResult getWebSizeTitle(){
        return webSizeInfoService.getWebSizeTitle();
    }

    /**
     * 获取网站访问量
     * @return
     */
    @GetMapping("/view_count")
    public ResponseResult getWebSizeViewCount(){
        return webSizeInfoService.getWebSizeViewCount();
    }

    /**
     * 获取SEO信息
     * @return
     */
    @GetMapping("/seo")
    public ResponseResult getSeo(){
        return webSizeInfoService.getSeoInfo();
    }

    /**
     * 获取轮播图列表
     * @return
     */
    @GetMapping("/looper")
    public ResponseResult getLooper(){
        return looperService.listLoops();
    }

    /**
     * 获取友情链接列表
     * @return
     */
    @GetMapping("/friend_link")
    public ResponseResult getFriendLink(){
        return friendLinkService.listFriendLink();
    }

    @PutMapping("view_count")
    public void updateViewCount(){
        webSizeInfoService.updateViewCount();
    }
}
