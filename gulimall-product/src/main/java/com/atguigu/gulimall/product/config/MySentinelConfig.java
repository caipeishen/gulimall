package com.atguigu.gulimall.product.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/20 14:29
 * @Description: 配置请求被限制以后的处理器
 */
@Configuration
public class MySentinelConfig {
    
    public MySentinelConfig(){
        WebCallbackManager.setUrlBlockHandler((request, response, exception) -> {
            R error = R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(JSON.toJSONString(error));
        });
    }
    
}
