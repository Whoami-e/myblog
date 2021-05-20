package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_friends
 * @author 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendLink implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 友情链接名称
     */
    private String name;

    /**
     * 友情链接logo
     */
    private String logo;

    /**
     * 友情链接
     */
    private String url;

    /**
     * 顺序
     */
    private Integer order = 1;

    /**
     * 友情链接状态:0表示不可用，1表示正常
     */
    private String state = "1";

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