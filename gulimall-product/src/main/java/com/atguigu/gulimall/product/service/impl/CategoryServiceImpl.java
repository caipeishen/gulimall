package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog3Vo;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    
    @Autowired
    private RedissonClient redissonClient;
    
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
    
    /**
     * 使用Cache集成Redis 更快速开发
     * 1、每一个需要缓存的数据我们都来指定装故到那个名字的缓存。【缓存的分区(按翮业务类型分)】
     * 2、@Cacheable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     *      1）、如果缓存中有，不调用改方法
     *      2）、key默认自动生成，缓存名字::simpleKey，将序列化后的数据放入redis中
     *      3）、默认ttl时间 -1
     *
     *    自定义
     *      1）、指定生成缓存使用的key，key属性指定，接受一个SpEL
     *      2）、指定缓存的数据存活时间，配置文件红修改ttl spring.cache.type.redis.time-to-live=3600000
     *      3）、将数据保存成JSON
     *
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys...");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    /**
     * 缓存穿透 查询为空的数据也设置缓存，并设置短的过期时间
     * 缓存雪崩 设置不同的过期时间 固定时长+随机时长
     * 缓存击穿 加锁
     *      1.getCatelogJson() 不加锁 使用redis做缓存(会有缓存击穿问题)
     *      2.getCatelogJsonFromDBWithLocalLock() 加本地锁 使用redis做缓存（如果不是分布式系统是可以的）
     *      3.getCatelogJsonFromDBWithRedisLock() UUID+redis锁+Lua 使用redis做缓存(解决分布式系统问题)
     *      4.getCatelogJsonFromDBWithRedissonLock() redisson加锁 自带原子性操作，还有看门狗机制，底层也是Lua脚本
     *
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
        String redisKey = "catelogJSON";
        String catelogJSON = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isBlank(catelogJSON)) {
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
            Map<String, List<Catelog2Vo>> parent_cid = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
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
    
    /**
     * redis没有数据 查询DB [本地锁解决方案]
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithLocalLock() {
        synchronized (this) {
            // 双重检查 是否有缓存
            return getDataFromDB();
        }
    }
    
    /**
     * 使用单传的redis分布式锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock() {
        // 1.占分布式锁  设置这个锁10秒自动删除 [原子操作]
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        
        if (lock) {
            // 2.设置过期时间加锁成功 获取数据释放锁 [分布式下必须是Lua脚本删锁,不然会因为业务处理时间、网络延迟等等引起数据还没返回锁过期或者返回的过程中过期 然后把别人的锁删了]
            Map<String, List<Catelog2Vo>> data;
            try {
                data = getDataFromDB();
            } finally {
//			stringRedisTemplate.delete("lock");
                String lockValue = stringRedisTemplate.opsForValue().get("lock");
                
                // 删除也必须是原子操作 Lua脚本操作 删除成功返回1 否则返回0
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 原子删锁
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        } else {
            // 重试加锁
            try {
                // 登上两百毫秒
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDBWithRedisLock();
        }
    }
    
    /**
     * 使用Redisson框架分布式锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedissonLock() {
        // 1、锁的名字。锁的粒度，越细越快。
        // 锁的粒度:具体缓存的是某个数据，11-号商品;product-11-Lock product-12-Lock
        RLock lock = redissonClient.getLock("catelogJSON-lock");
        lock.lock(30, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> data;
        try {
            data = getDataFromDB();
        } finally {
            // 2.解锁
            lock.unlock();
        }
        return getCatelogJsonFromDBWithRedisLock();
    }
    
    /**
     * redis无缓存 查询数据库
     */
    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String redisKey = "catelogJSON";
        String catelogJSON = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.isEmpty(catelogJSON)) {
            return JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        // 优化：将查询变为一次
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        System.out.println("查询数据库...");
        
        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
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
        // 优化：查询到数据库就再锁还没结束之前放入缓存
        stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
        return parent_cid;
    }

}