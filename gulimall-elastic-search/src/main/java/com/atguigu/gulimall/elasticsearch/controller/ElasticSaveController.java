package com.atguigu.gulimall.elasticsearch.controller;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.elasticsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/2/15 19:05
 * @Description: ElasticSearch保存
 **/
@Slf4j
@RequestMapping("/elasticsearch/save")
@RestController
public class ElasticSaveController {

	@Autowired
	private ProductSaveService productSaveService;

	/**
	 * 上架商品
	 */
	@PostMapping("/product")
	public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
		boolean status;
		try {
			status = productSaveService.productStatusUp(skuEsModels);
		} catch (IOException e) {
			log.error("ElasticSaveController商品上架错误: {}", e);
			return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
		}
		if(!status){
			return R.ok();
		}
		return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
	}
}
