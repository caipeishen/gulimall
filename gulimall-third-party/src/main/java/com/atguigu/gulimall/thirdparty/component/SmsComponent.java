package com.atguigu.gulimall.thirdparty.component;

import com.atguigu.common.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/8 21:38
 * @Description:
 **/
@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SmsComponent {

    private String host;

    private String path;

    private String smsSignId;

    private String templateId;

    private String appCode;

    public String sendSmsCode(String phone, String code){
        // 和教程中的发送短信不一个（教程中的必须要企业认证），购买地址【https://market.aliyun.com/products/57126001/cmapi00037415.html?spm=5176.2020520132.101.2.40ec7218nZVAda#sku=yuncode3141500001】
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + this.appCode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:" + code + ",**minute**:3");
        querys.put("smsSignId", this.smsSignId);
        querys.put("templateId", this.templateId);
        Map<String, String> bodys = new HashMap<String, String>();
        HttpResponse response = null;
        try {
            response = HttpUtils.doPost(this.host, this.path, method, headers, querys, bodys);
            //获取response的body
            if(response.getStatusLine().getStatusCode() == 200){
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail_" + response.getStatusLine().getStatusCode();
    }

}
