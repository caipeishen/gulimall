package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/8 20:35
 * @Description: 配置中心
 **/
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/register.html").setViewName("register");
    }

}
