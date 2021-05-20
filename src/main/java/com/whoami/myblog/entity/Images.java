package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * tb_images
 * @author 
 */
@Data
public class Images implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 路径
     */
    private String url;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 图片类型
     */
    private String contentType;

    /**
     * 原名称
     */
    private String name;

    /**
     * 状态（0表示删除，1表正常）
     */
    private String state;

    /**
     * 图片MD5值
     */
    private String md5;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 来源：article,friendlink,loop
     */
    private String original;

    private static final long serialVersionUID = 1L;
}