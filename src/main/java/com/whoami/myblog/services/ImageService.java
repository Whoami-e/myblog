package com.whoami.myblog.services;

import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ImageService {
    ResponseResult uploadImage(MultipartFile file, String original);

    void viewImage(HttpServletResponse response, String imageId) throws IOException;

    ResponseResult listImages(Page page,String state, String original);

    ResponseResult deleteById(String imageId);

    void createQrCode(String code, HttpServletResponse response, HttpServletRequest request);

    ResponseResult getRightImage(String imageId);

    ResponseResult uploadMd(MultipartFile file);
}
