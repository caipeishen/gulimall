package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.exception.NotStockException;
import com.atguigu.common.to.es.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");

                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }


            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    /**
     * 这里存过库存数量
     * SELECT SUM(stock - stock_locked) FROM `wms_ware_sku` WHERE sku_id = 1
     */
    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(id -> {
            SkuHasStockVo stockVo = new SkuHasStockVo();

            // 查询当前sku的总库存量
            stockVo.setSkuId(id);
            // 这里库存可能为null 要避免空指针异常
            stockVo.setHasStock(baseMapper.getSkuStock(id)==null?false:true);
            return stockVo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = NotStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 当定库存之前先保存订单 以便后来消息撤回
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        this.orderTaskService.save(taskEntity);
        // [理论上]1. 按照下单的收获地址 找到一个就近仓库, 锁定库存
        // [实际上]1. 找到每一个商品在那个一个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock hasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            hasStock.setSkuId(skuId);
            // 查询这两个商品在哪有库存
            List<Long> wareIds = this.wareSkuDao.listWareIdHasSkuStock(skuId);
            hasStock.setWareId(wareIds);
            hasStock.setNum(item.getCount());
            return hasStock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = true;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if(wareIds == null || wareIds.size() == 0){
                // 没有任何仓库有这个库存
                throw new NotStockException(skuId.toString());
            }
            // 如果每一个商品都锁定成功 将当前商品锁定了几件的工作单记录发送给MQ
            // 如果锁定失败 前面保存的工作单信息回滚了
            for (Long wareId : wareIds) {
                // 成功就返回 1 失败返回0
                Long count = this.wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if(count == 1){
                    skuStocked = false;
                    break;
                }
                // 当前仓库锁定失败 重试下一个仓库
            }
            if(skuStocked){
                // 当前商品在所有仓库都没锁柱
                throw new NotStockException(skuId.toString());
            }
        }
        // 3.全部锁定成功
        return true;
    }

    @Data
    class SkuWareHasStock{

        private Long skuId;

        private List<Long> wareId;

        private Integer num;
    }

}