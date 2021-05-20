package com.whoami.myblog.controller.portal;

import com.whoami.myblog.entity.Page;
import com.whoami.myblog.response.ResponseResult;
import com.whoami.myblog.services.SolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/search")
public class SearchPortalApi {

    @Autowired
    private SolrService solrService;

    @GetMapping
    public ResponseResult doSearch(@RequestParam("keyword")String keyword,
                                   @RequestParam("pageNum") int pageNum,
                                   @RequestParam("pageSize") int pageSize,
                                   @RequestParam(value = "categoryId",required = false) String categoryId,
                                   @RequestParam(value = "sort",required = false) Integer sort){
        Page page = new Page(pageNum,pageSize);
        return solrService.doSearch(keyword,page,categoryId,sort);
    }
}
