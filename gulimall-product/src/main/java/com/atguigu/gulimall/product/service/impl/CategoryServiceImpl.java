package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        
        return new PageUtils(page);
    }
    
    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类数据
        List<CategoryEntity> allCategoryList = this.baseMapper.selectList(null);
        
        // 组成树形结构
        List<CategoryEntity> categoryList = allCategoryList.stream()
                // 先获取一级分类
                .filter(cate -> cate.getParentCid() == 0)
                .map(cate -> {
                    // 循环递归取子分类
                    cate.setChildren(this.getChildrenList(cate.getCatId(), allCategoryList));
                    return cate;
                }).sorted((cate1, cate2) ->
                    // 排序
                    (cate1.getSort() == null ? 0 : cate1.getSort()) - (cate2.getSort() == null ? 0 : cate2.getSort())
                )
                .collect(Collectors.toList());
        return categoryList;
    }
    
    /**
     * 递归取子分类
     *
     * @param pId
     * @param allCategoryList
     * @return
     */
    private List<CategoryEntity> getChildrenList(Long pId, List<CategoryEntity> allCategoryList) {
        List<CategoryEntity> childrenList = allCategoryList.stream()
                // 过滤出子分类来进行处理
                .filter(cate -> pId.equals(cate.getParentCid()))
                .map(cate -> {
                    // 递归设置子分类
                    cate.setChildren(this.getChildrenList(cate.getCatId(), allCategoryList));
                    return cate;
                })
                .sorted((cate1, cate2) ->
                    // 排序
                    (cate1.getSort() == null ? 0 : cate1.getSort()) - (cate2.getSort() == null ? 0 : cate2.getSort())
                )
                .collect(Collectors.toList());
        return childrenList;
    }
    
}