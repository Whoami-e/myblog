package com.whoami.myblog.services.Impl;

import com.whoami.myblog.utils.EmailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    /**
     * 异步发送邮件
     * @param code
     * @param emailAddress
     * @throws Exception
     */
    @Async
    public void sendEmailCode(String code, String emailAddress) throws Exception {
        EmailSender.sendRegisterVerifyCode(code,emailAddress);
    }
}
