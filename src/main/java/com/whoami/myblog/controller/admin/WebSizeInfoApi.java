package com.whoami.myblog.controller.admin;

import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.WebSizeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/web_size_info")
public class WebSizeInfoApi {

    @Autowired
    private WebSizeInfoService webSizeInfoService;

    /**
     * 获取网站title
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("title")
    public ResponseResult getWebSizeTitle(){
        return webSizeInfoService.getWebSizeTitle();
    }

    /**
     * 修改网站title
     * @param title
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("title")
    public ResponseResult putWebSizeTitle(@RequestParam("title")String title){
        return webSizeInfoService.putWebSizeTitle(title);
    }

    /**
     * 获取网站SEO信息
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/seo")
    public ResponseResult getSeoInfo(){
        return webSizeInfoService.getSeoInfo();
    }

    /**
     * 修改网站SEO信息
     * @param keywords
     * @param description
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/seo")
    public ResponseResult putSeoInfo(@RequestParam("keywords")String keywords,@RequestParam("description")String description){
        return webSizeInfoService.putSeoInfo(keywords,description);
    }

    /**
     * 获取网站统计信息
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/view_count")
    public ResponseResult getWebSizeViewCount(){
        return webSizeInfoService.getWebSizeViewCount();
    }
}
