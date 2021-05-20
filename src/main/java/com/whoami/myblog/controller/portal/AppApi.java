package com.whoami.myblog.controller.portal;

import com.whoami.myblog.response.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/portal/app")
public class AppApi {

    @GetMapping("/{code}")
    public void downloadAppForThirdPartScan(@PathVariable("code") String code,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {

    }
}
