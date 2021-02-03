package com.atguigu.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 流程：该路由，如果断言为ture，则进行filter过滤
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) // 网关不需要数据源(不排除会报错)
public class GulimallGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GulimallGatewayApplication.class, args);
    }
    
}
