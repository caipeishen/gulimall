package com.atguigu.gulimall.product;

import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {
    
    @Autowired
    private BrandService brandService;
    
    @Autowired
    private OSSClient ossClient;
    
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
        ossClient.putObject("gulimall-ferris", "头像2.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传成功...");
    }
    
}
