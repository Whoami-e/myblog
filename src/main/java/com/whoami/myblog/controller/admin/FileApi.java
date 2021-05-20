package com.whoami.myblog.controller.admin;

import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/file")
public class FileApi {

    @Autowired
    private ImageService imageService;

    /**
     * 上传MD文件
     * @param file
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping("/uploadMd")
    public ResponseResult uploadMd(@RequestParam("file") MultipartFile file){
        return imageService.uploadMd(file);
    }
}
