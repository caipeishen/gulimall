package com.atguigu.gulimall.sso.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 1.client1访问受保护资源，通过token从redis中获取信息(没有token)，也就获取不到
 * 2.这时跳转到ssoserver认证中心，并且携带参数redirect_url(访问client1的url)
 * 3.认证中心判断当前是否曾登录过(认证服务中心的cookie[ssoserver.com域名的cookie])，如果没有跳转到登录界面
 * 4.登录完成后，生成token存入redis并跳转到redirect_url(client1)同时拼接参数 ?token=
 * 5.跳到了client1，然后再通过token从redis获取，发现有，则执行业务
 *
 * 6.client2访问受保护资源，通过token从redis中获取信息(没有token，因为上面的步骤token是返回给client1的)，也就获取不到
 * 7.这时跳转到ssoserver认证中心，并且携带参数redirect_url(访问client2的url)
 * 8.认证中心判断当前是否曾登录过(认证服务中心的cookie[ssoserver.com域名的cookie])，如果没有跳转到登录界面，如果有则表示曾登陆过，再通过cookie从redis中获取，获取到则表示没有过期
 * 9.再跳转到client2同时拼接参数 ?token=
 *
 *
 * 核心：
 *  1.给登录服务器留下登录痕迹
 *  2.登录服务器重定向url的时候，要将token信息带上
 *  3.其他系统要处理url上的关键token，去redis验证得到登录的信息
 */
@SpringBootApplication
public class GulimallTestSsoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallTestSsoServerApplication.class, args);
    }

}
