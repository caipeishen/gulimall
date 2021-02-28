package com.atguigu.gulimall.search.service;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @Author: Cai Peishen
 * @Date: 2021/2/28 14:44
 * @Description:
 **/
public interface MallSearchService {

    SearchResult search(SearchParam searchParam);

}
