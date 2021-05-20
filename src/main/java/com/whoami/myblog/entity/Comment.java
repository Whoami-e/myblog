package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * tb_comment
 * @author 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Comment implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 父内容
     */
    private String parentContent;

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论用户的ID
     */
    private String userId;

    /**
     * 评论用户的头像
     */
    private String userAvatar;

    /**
     * 评论用户的名称
     */
    private String userName;

    /**
     * 状态（0表示删除，1表示正常）
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