package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {
    
    @Autowired
    private BrandService brandService;
    
    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("Cai Peishen");
//        brandService.save(brandEntity);
    
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<BrandEntity>();
        queryWrapper.eq("NAME","Cai Peishen");
        List<BrandEntity> list = brandService.list(queryWrapper);
        list.forEach(brand -> {
            System.out.println(brand.getName());
        });
    
    }
    
}
