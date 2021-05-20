package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_labels
 * @author 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Labels implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 数量
     */
    private long count;

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