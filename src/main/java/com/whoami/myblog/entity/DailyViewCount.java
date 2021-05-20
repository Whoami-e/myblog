package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * tb_daily_view_count
 * @author 
 */
@Data
public class DailyViewCount implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 每天浏览量
     */
    private Integer viewCount;

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