package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_notes
 * @author 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notes implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 速记内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}