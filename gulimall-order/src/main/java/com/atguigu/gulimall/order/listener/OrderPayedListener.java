package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.conf.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 异步接收支付宝成功回调
 *
 * 收单解决非正常BUG
 * 1、订单在支付页，不支付，一直刷新，订单过期了才支付，订单状态改为已支付了，但是库 存解锁了。 【使用支付宝自动收单功能解决。只要一段时间不支付，就不能支付了。】
 * 2、由于时延等问题。订单解锁完成，正在解锁库存的时候，异步通知才到。【订单解锁，手动调用收单】
 * 3、网络阻塞问题，订单支付成功的异步通知一直不到达【查询订单列表时，ajax获取当前未支付的订单状态，查询订单状态时，再获取一下支付宝 此订单的状态】
 * 4、其他各种问题，【每天晚上闲时下载支付宝对账单，一一进行对账】
 */
@RestController
public class OrderPayedListener {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;
    
    /**
     * 支付宝异步通知
     * @param request
     * @param payAsyncVo
     * @return
     * @throws AlipayApiException
     */
    @PostMapping("/payed/notify")
    public String handlerAlipay(HttpServletRequest request, PayAsyncVo payAsyncVo) throws AlipayApiException {
        System.out.println("收到支付宝异步通知******************");
        // 只要收到支付宝的异步通知，返回 success 支付宝便不再通知
        // 获取支付宝POST过来反馈信息
        // 需要验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if (signVerified){
            System.out.println("支付宝异步通知验签成功");
            //修改订单状态
            this.orderService.handlerPayResult(payAsyncVo);
            return "success";
        }else {
            System.out.println("支付宝异步通知验签失败");
            return "error";
        }
    }

}
