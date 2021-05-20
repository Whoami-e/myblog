package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_refresh_token
 * @author 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * token
     */
    private String refreshToken;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * tokenKey
     */
    private String tokenKey;

    /**
     * mobileTokenKey
     */
    private String mobileTokenKey;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}