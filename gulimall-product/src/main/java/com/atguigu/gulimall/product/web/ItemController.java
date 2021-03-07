package com.atguigu.gulimall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @Author: Cai Peishen
 * @Date: 2021/3/7 15:01
 * @Description: 商品详情页
 **/
@Controller
public class ItemController {

    @RequestMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        System.out.println("商品详情页skuId：" + skuId);

        model.addAttribute("item", null);
        return "item";
    }

}
