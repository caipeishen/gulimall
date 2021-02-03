package com.atguigu.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 远程调用别的服务
 * 1.引入open-feign pom.xml
 * 2.开启远程调用功能
 * 3.编写feign远程调用类(1.调用的服务 2.调用的具体接口)
 */
@EnableFeignClients(basePackages = "com.atguigu.gulimall.member.feign")
@SpringBootApplication
public class GulimallMemberApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }
    
}
