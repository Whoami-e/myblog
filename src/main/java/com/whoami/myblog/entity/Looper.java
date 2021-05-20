package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_looper
 * @author 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Looper implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 顺序
     */
    private Integer order = 1;

    /**
     * 状态：0表示不可用，1表示正常
     */
    private String state = "1";

    /**
     * 目标URL
     */
    private String targetUrl;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}