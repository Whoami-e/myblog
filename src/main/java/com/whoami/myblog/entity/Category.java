package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_categories
 * @author 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 描述
     */
    private String description;

    /**
     * 顺序
     */
    private Integer order = 1;

    /**
     * 状态：0表示不使用，1表示正常
     */
    private String status;

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