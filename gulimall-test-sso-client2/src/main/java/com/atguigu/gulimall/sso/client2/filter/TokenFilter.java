package com.atguigu.gulimall.sso.client2.filter;

import com.atguigu.gulimall.sso.conf.MyConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/13 21:44
 * @Description: 自定义过滤器：过滤无token数据
 **/
public class TokenFilter implements Filter {

    @Value("${sso.server.login-url}")
    private String loginUrl;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 取值：MyFilterConfig注入时存放的数据，现在采用@Bean注入的方式，直接从容器中获取数据
        // this.loginUrl = filterConfig.getInitParameter(Conf.LOGIN_URL);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String token = req.getParameter(MyConf.TOKEN);
        String uri = req.getRequestURL().toString();
        String loginPageUrl = this.loginUrl.concat("?redirect_url=").concat(uri);

        if (StringUtils.isEmpty(token)) {
            res.sendRedirect(loginPageUrl);
        } else {
            String user = this.stringRedisTemplate.opsForValue().get(token);
            if (StringUtils.isEmpty(user)) {
                res.sendRedirect(loginPageUrl);
            }
            req.setAttribute(MyConf.TOKEN, user);
        }

        // already login, allow
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

}