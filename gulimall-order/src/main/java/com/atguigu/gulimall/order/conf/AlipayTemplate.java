package com.atguigu.gulimall.order.conf;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000117635428";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCV1t/hLtxw6DiN7XC1EGAdl4S5DWH9bDE4GrgQdcxeQAU0977yCOJBeio7oN/bdkvACxqyTbKFh21Qg49AU4QzwiNUeEZ7yTRgO+dwBaIsHYrQmBkNN+IKaEOFjX2geVxH10H80QjWYzT2QN0oMUQRmkri/EZTozYHFKKu8ULE6ZRUluz+ZK2mv0gsZ80x36wPCcCicdeq10em+yAje8dafeF8TKKMAggl6bM7HoQ8JyzW/2pPcSy2T4jWNdgq2XBnOSFe0exIgbN83owUtHZRFWUfprWvCwQYzrA6jhFpFbo4U/3P03mhaUXV1QnCSJ/kYaP0vWJL70ZGYh3BohRNAgMBAAECggEAepO+sGtygJcodMrSUf0kc/DY7b3J9NRGa3xIKYcEefHrkIo/1aZ42VHELOWNW+6pR2COJWE0ctH4Fk6ZqOjplKHz3w1Js8hEpZk72sNxCJ/eSIi0rcCSlSr4WdAG/nlpiYfJ/rVOgnvD/tPPWCr9YV0UeB4ithclgqNZ9r19frHAZB4hfybf3LhAoWk96AczRr6uLxs1YpJwqVsdfyJlHJufx3RvVvgWXOWBRXf8XX68SbOqbNMJcdDzGA+WQv8AizFrQqLqMKKs9VLGUofPP185MTOSHFoPxE2/mxS4xrVeGzac82zXayAZ7JK5Ab/0RaNGuMaUEd4ax2uZmaXrQQKBgQD7V8fuLpMNw3yO2dhkFCoTuyyTuS99sIvN132SP1mKcFoL7LdA4uRNP9TJ2aP4S8P2GzMgViOj3dhba9JEfxBf7lQ/y8VV025VUDSnzX7LykfiWZMBKc450Vj2SiaDrETKXWLAUtIDLjxpMRWn3q6Mec0Dk7dizlK2vcgLAbvNcQKBgQCYnZ87tw8UB/Nt4m4uHYcDikF5TVEOVq5+OnNdfa7Exs0/fP0iAPChBL9DKhyegazBB1kAbqyKaZbMMUqgSdmFZBlq+nqQwhd2QmaJrTT6xhGaltbjx1OuRbII1ioepCmPpEtUHxaGmo+48o6pIk6M2HWY2UAw8pU80bgRx3N2nQKBgQDBhkRFq4zEszlL0IZiAHipepwHHjZn47CxjHN+UNLD2mArAFfVWR4KMNTJmo87FjNLYoQKKOkGOkFlmu5qmR9ljd9Gw8IspEA25iu07G/tv9F7ghbDdYn5UKzb7hobll/dJOxtQxe7JTWUlRukhrwFI59sWqeAJ3beh9/oQax1sQKBgGskFRq+DBba8hnNod48GoHwuMBo+COh1QSHEmCrEFvS2Vrd63PIxyWziHalhMv+a4JkHyZ/jB/rutMFM8fNsKjFulZxHESuDfyW5xd2gsAe91LY3GmykNvC0BcGCzHRu9+8zqly3cSHk6iamG7Cx/R3Li+tLTRvyTeWX6wTosq9AoGBAIkfFg5PNfNsz49DwarwNJORfE136QAJ1Rhzu5+jIkLbgp5Ik16I9SCHbIk+AQktLgzPIaVAVquuYA6tqU/frCoKNhm70R4sV6VI2zqxEwQMS10Ek6wkGkAYD4vIZQzfz/7uyVwfHcRMq+2rEBkCZcmtNCZQcZYGIp2JoRn6H4w0";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlq+lOpYp6hFhHKgcSCu8mB4l6G6IjCDh1gpdf/fQDDMl88NDRmhY6A6xMOUwBBPcX0G7dsjG9v1YlbwK4xq0jx56gvx2BFvCoVCK5pYRqupNOh65NH5UzUiHyVt6gNG9fkMJI5aXMj4eXFxY7uJNyb/6A6tPuH9cEKNpPsKExqE+bVhf6+9EZFtJuGkQf9mS2hKHT+rj/FGGmFfHwuEJ96J4fLRTiC4UUgnGvaDmRimXCsVr1hrBUqvR6Jeln4+kUIOZlv8846Wk/4XFWSOtE1YwqouGFKERbA1fkkC4crlsHLQgIOFI7ohp/mjqhcXoRR830V8kuxWVhOtTpWE2GQIDAQAB";

    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url="http://nxz9r8.natappfree.cc/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url="http://order.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    
    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                +"\"timeout_express\":\"1m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;
    }
}
