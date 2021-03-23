package com.atguigu.gulimall.order.conf;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 11:02
 * @Description: 解决openFeign丢失请求头
 */
@Configuration
public class GuliFeignConfig {
	@Bean("requestInterceptor")
	public RequestInterceptor requestInterceptor(){
		// Feign在远程调用之前都会先经过这个方法
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				// RequestContextHolder拿到刚进来这个请求
				ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
				if(attributes != null){
					HttpServletRequest request = attributes.getRequest(); // 老请求
					if(request != null){
						// 同步请求头数据
						String cookie = request.getHeader("Cookie");
						// 给新请求同步Cookie
						template.header("Cookie", cookie);
					}
				}
			}
		};
	}
}
