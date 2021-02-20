package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog3Vo;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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

    @Override
    public void removeCateByIds(List<Long> catIds) {
        // TODO 只删除未使用的分类
        // 逻辑删除
        baseMapper.deleteBatchIds(catIds);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    /**
     * TODO 产生堆外内存溢出:OutOfDirectMemoryError
     * 1.springboot2.o以后默认使用Lettuce作为操作redis的客户端。它使用netty进行网络通信
     * 2.lettuce的bug导致netty堆外内存溢出-Xmx300m; netty如果没有指定堆外内存，默认使用-Xm×300m 可以通过-Dio.netty.maxDirectMemory进行设置
     * 解决方案:不能使用-Dio.netty.maxDirectMemory只去调大堆外内存。
     *      1.升级Lettuce客户端
     *      2.切换使用jedis
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        String redisKey = "getCatelogJson";
        String getCatelogJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isBlank(getCatelogJson)) {
            List<CategoryEntity> entityList = baseMapper.selectList(null);
            // 查询所有一级分类
            List<CategoryEntity> level1 = this.getCategoryEntities(entityList, 0L);
            Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // 拿到每一个一级分类 然后查询他们的二级分类
                List<CategoryEntity> entities = this.getCategoryEntities(entityList, v.getCatId());
                List<Catelog2Vo> catelog2Vos = null;
                if (entities != null) {
                    catelog2Vos = entities.stream().map(l2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                        // 找当前二级分类的三级分类
                        List<CategoryEntity> level3 = this.getCategoryEntities(entityList, l2.getCatId());
                        // 三级分类有数据的情况下
                        if (level3 != null) {
                            List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(catalog3Vos);
                        }
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return catelog2Vos;
            }));
            String cateJSON = JSON.toJSONString(parent_cid);
            stringRedisTemplate.opsForValue().set(redisKey, cateJSON);
            return parent_cid;
        } else {
            Map<String, List<Catelog2Vo>> parent_cid = JSON.parseObject(getCatelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
            return parent_cid;
        }
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


    /**
     * 获取分类层级ID：2,25,225
     *
     * @param catelogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    /**
     * 第一次查询的所有 CategoryEntity 然后根据 parent_cid去这里找
     */
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> entityList, Long parent_cid) {
        return entityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }

}