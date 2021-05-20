package com.whoami.myblog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult implements Serializable {

    @Field("id")
    private String id;

    @Field("blog_content")
    private String blogContent;

    @Field("blog_create_time")
    private Date blogCreateTime;

    @Field("blog_labels")
    private String blogLabels;

    @Field("blog_url")
    private String blogUrl;

    @Field("blog_title")
    private String blogTitle;

    @Field("blog_view_count")
    private int blogViewCount;
}
