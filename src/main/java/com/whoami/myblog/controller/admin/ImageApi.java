package com.whoami.myblog.controller.admin;

import com.whoami.myblog.entity.Page;
import com.whoami.myblog.interceptor.CheckTooFrequentCommit;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/admin/image")
public class ImageApi {

    @Autowired
    private ImageService imageService;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping("/{original}")
    public ResponseResult uploadImage(@PathVariable("original") String original ,@RequestParam("file")MultipartFile file){
        return imageService.uploadImage(file,original);
    }

    /**
     * 删除图片(改为禁用状态)
     * @param imageId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{imageId}")
    public ResponseResult deleteImage(@PathVariable("imageId")String imageId){
        return imageService.deleteById(imageId);
    }

    /**
     * 恢复正常
     * @param imageId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{imageId}")
    public ResponseResult getRightImage(@PathVariable("imageId")String imageId){
        return imageService.getRightImage(imageId);
    }

    /**
     * 显示图片
     * @param response
     * @param imageId 图片ID
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response, @PathVariable("imageId")String imageId){
        try {
            imageService.viewImage(response,imageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取正常图片列表
     * @param pageNum  页码
     * @param pageSize 页容量
     * @param original 图片来源
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{pageNum}/{pageSize}")
    public ResponseResult listImage(@PathVariable("pageNum") int pageNum,
                                    @PathVariable("pageSize") int pageSize,
                                    @RequestParam(value = "original",required = false) String original){
        Page page = new Page(pageNum,pageSize);
        return imageService.listImages(page,"1",original);
    }

    /**
     * 获取全部图片
     * @param pageNum  页码
     * @param pageSize 页容量
     * @param original 图片来源
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list-all/{pageNum}/{pageSize}")
    public ResponseResult listImageManagement(@PathVariable("pageNum") int pageNum,
                                    @PathVariable("pageSize") int pageSize,
                                    @RequestParam(value = "original",required = false) String original){
        Page page = new Page(pageNum,pageSize);
        return imageService.listImages(page,"",original);
    }
}
