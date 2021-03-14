package com.atguigu.gulimall.sso.server.filter;

import com.atguigu.gulimall.sso.conf.MyConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
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

    @Value("${sso.server.excluded-paths}")
    private String excludedPaths;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 取值：MyFilterConfig注入时存放的数据，现在采用@Bean注入的方式，直接从容器中获取数据
        // this.loginUrl = filterConfig.getInitParameter(Conf.LOGIN_URL);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURL().toString();
        String loginPageUrl = this.loginUrl.concat("?redirect_url=").concat(uri);

        // make url
        String servletPath = req.getServletPath();

        // excluded path check
        if (excludedPaths!=null && excludedPaths.trim().length()>0) {
            for (String excludedPath : excludedPaths.split(",")) {
                String uriPattern = excludedPath.trim();

                // 支持ANT表达式
                if (antPathMatcher.match(uriPattern, servletPath)) {
                    // excluded path, allow
                    chain.doFilter(request, response);
                    return;
                }

            }
        }

        // 判断是否曾登陆过(cookie)
        String ssoToken = this.getCookieValue(MyConf.COOKIE_SSO_TOKEN, req);
        if (StringUtils.isEmpty(ssoToken)) {
            res.sendRedirect(loginPageUrl);
        } else {
            String user = this.stringRedisTemplate.opsForValue().get(ssoToken);
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

    /**
     * 从cookie中获取
     * @param request
     * @return
     */
    private String getCookieValue (String cookieName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equalsIgnoreCase(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}