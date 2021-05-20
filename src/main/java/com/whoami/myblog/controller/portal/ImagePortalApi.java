package com.whoami.myblog.controller.portal;

import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/portal/image")
public class ImagePortalApi {

    @Autowired
    private ImageService imageService;

    /**
     * 显示图片
     * @param response
     * @param imageId
     */
    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response, @PathVariable("imageId")String imageId){
        try {
            imageService.viewImage(response,imageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/qr-code/{code}")
    public void getQrCodeImage(@PathVariable("code") String code, HttpServletResponse response, HttpServletRequest request){
        imageService.createQrCode(code,response,request);
    }
}
