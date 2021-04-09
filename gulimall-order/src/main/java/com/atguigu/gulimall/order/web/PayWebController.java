package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.conf.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;
    
    /**
     * 点击支付宝支付
     *
     * 账号：dnbpww3227@sandbox.com
     * 登录密码：111111
     * 支付密码：111111
     */
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        System.out.println("接收到订单信息orderSn："+orderSn);
        PayVo payVo = this.orderService.getOrderPay(orderSn);
        String pay = this.alipayTemplate.pay(payVo);
        return pay;
    }
    
    
    /**
     * 获取当前用户的所有订单
     * @return
     */
    @RequestMapping("/memberOrder.html")
    public String memberOrder(@RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum, Model model){
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum.toString());
        PageUtils page = this.orderService.getMemberOrderPage(params);
        model.addAttribute("pageUtil", page);
        return "list";
    }
    
}
