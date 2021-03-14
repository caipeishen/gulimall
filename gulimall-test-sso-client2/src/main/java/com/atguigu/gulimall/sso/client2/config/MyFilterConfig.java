package com.atguigu.gulimall.sso.client2.config;

import com.atguigu.gulimall.sso.client2.filter.TokenFilter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/13 21:45
 * @Description:
 **/
@Configuration
public class MyFilterConfig implements DisposableBean {

    @Value("${sso.server.login-url}")
    private String loginUrl;

    @Bean
    Filter tokenFilter() {
        return new TokenFilter();
    }

    @Bean
    public FilterRegistrationBean registFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("TokenFilter");
        registration.setFilter(tokenFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);

        // 向过滤器中存值，在过滤器中的init可以取filterConfig.getInitParameter(Conf.LOGIN_URL)；现在使用@bean注入，这样在过滤器中可以直接注入bean
        // registration.setFilter(new TokenFilter());
        // registration.addInitParameter(Conf.LOGIN_URL, this.loginUrl);
        return registration;
    }

    @Override
    public void destroy() throws Exception {

    }
}