package com.whoami.myblog.entity;

import com.github.pagehelper.PageInfo;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
public class PageList<T> implements Serializable {

    //当前页码
    private long pageNum;

    public PageList(long pageNum, long totalCount, long pageSize) {
        this.pageNum = pageNum;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.totalPage = (long) (this.totalCount / (this.pageSize * 1.0f) + 0.9f);
        //是否是第一页或最后一页
        this.isFirst = this.pageNum == 1;
        this.isLast = this.pageNum == totalPage;
    }

    //总数量
    private long totalCount;
    //页容量
    private long pageSize;
    //总页数
    private long totalPage;
    //是否是第一页
    private boolean isFirst;
    //是否是最后一页
    private boolean isLast;
    //数据
    private List<T> contents;

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }


    public void parsePage(PageInfo<T> commentPageInfo) {
        setContents(commentPageInfo.getList());
        setFirst(commentPageInfo.isIsFirstPage());
        setLast(commentPageInfo.isIsLastPage());
        setPageNum(commentPageInfo.getPageNum());
        setTotalPage(commentPageInfo.getPages());
        setTotalCount(commentPageInfo.getList().size());
        setPageSize(commentPageInfo.getSize());
    }
}
