package com.whoami.myblog.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * tb_article
 * @author 
 */
@Data
public class Article implements Serializable {
    /**
     * ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 类型（0表示富文本，1表示markdown）
     */
    private String type;

    /**
     * 状态（0表示已发布，1表示草稿，2表示删除，3表示置顶）
     */
    private String state;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 标签
     */
    private String labels;

    /**
     * 阅读数量
     */
    private long viewCount = 0L;

    /**
     * 发布时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 封面
     */
    private String cover;

    private List<String> labelList = new ArrayList<>();

    public String getLabels() {
        this.labelList.clear();
        if (this.labels != null) {

            if (!this.labels.contains("-")) {
                this.labelList.add(this.labels);
            } else {
                String[] split = this.labels.split("-");
                List<String> strings = Arrays.asList(split);
                this.labelList.addAll(strings);
            }
        }
        return labels;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", userId='" + userId + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                ", userName='" + userName + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                ", summary='" + summary + '\'' +
                ", labels='" + labels + '\'' +
                ", viewCount=" + viewCount +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", cover='" + cover + '\'' +
                ", labelList=" + labelList +
                '}';
    }

    private static final long serialVersionUID = 1L;
}