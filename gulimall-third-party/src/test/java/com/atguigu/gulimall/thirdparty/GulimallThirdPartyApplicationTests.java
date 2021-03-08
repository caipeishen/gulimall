package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GulimallThirdPartyApplicationTests {
    
    @Autowired
    private OSSClient ossClient;

    @Resource
    private SmsComponent smsComponent;

    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucketName;

    @Test
    public void SendCodeTest(){
        String result = smsComponent.sendSmsCode("18848848551","666666");
        System.out.println(result);
    }

    @Test
    public void sendSms(){
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appCode = "e8fe660ecc7a4982a9ab6be0be4b6127";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE e8fe660ecc7a4982a9ab6be0be4b6127
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "18848848551");
        querys.put("param", "**code**:12345,**minute**:5");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();
        //JDK 1.8示例代码请在这里下载：  http://code.fegine.com/Tools.zip
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            //System.out.println(response.toString());如不输出json, 请打开这行代码，打印调试头部状态码。
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
//    void testOSS() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "https://oss-cn-shanghai.aliyuncs.com";
//        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录RAM控制台创建RAM账号。
//        String accessKeyId = "";
//        String accessKeySecret = "";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 获取文件流
//        InputStream inputStream = new FileInputStream("C:\\Users\\peish\\Pictures\\Camera Roll\\头像3.JPG");
//
//        // bucket、fileName、fileStream
//        ossClient.putObject("gulimall-ferris", "头像.jpg", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//
//        System.out.println("上传成功...");
//    }
    
    @Test
    void testSpringCloudAlibabaOSS() throws FileNotFoundException {
        // 获取文件流
        InputStream inputStream = new FileInputStream("C:\\Users\\peish\\Pictures\\Camera Roll\\头像3.JPG");
        
        // bucket、fileName、fileStream
        ossClient.putObject("gulimall-ferris", "头像4.jpg", inputStream);
        
        // 关闭OSSClient。
        ossClient.shutdown();
        
        System.out.println("上传成功...");
    }
    
    
}
