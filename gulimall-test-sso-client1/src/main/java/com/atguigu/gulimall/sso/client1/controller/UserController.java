package com.atguigu.gulimall.sso.client1.controller;

import com.atguigu.gulimall.sso.conf.MyConf;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/13 21:42
 * @Description:
 **/
@RestController
public class UserController {

    @RequestMapping("/userInfo")
    private String getUserInfo(HttpServletRequest request) {
        String user = (String)request.getAttribute(MyConf.TOKEN);
        return user;
    }

}
