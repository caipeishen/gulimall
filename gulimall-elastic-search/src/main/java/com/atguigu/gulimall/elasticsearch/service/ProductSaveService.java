package com.atguigu.gulimall.elasticsearch.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/2/19 14:11
 * @Description:
 **/
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;

}
