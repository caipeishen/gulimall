package com.atguigu.gulimall.sso.server.controller;

import com.atguigu.gulimall.sso.conf.MyConf;
import com.atguigu.gulimall.sso.server.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/13 23:01
 * @Description:
 **/
@Controller
public class UserController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(HttpServletRequest request){
        String user = (String)request.getAttribute(MyConf.TOKEN);
        return user;
    }

    @GetMapping("/login.html")
    public String loginPage(User user, Model model, @CookieValue(value = MyConf.COOKIE_SSO_TOKEN,required = false) String sso_token){
        // 有人登录过
        if(!StringUtils.isEmpty(sso_token)){
            String userJSON = this.stringRedisTemplate.opsForValue().get(sso_token);
            if (!StringUtils.isEmpty(userJSON)) {
                // 重点：当认证服务域名下含有cookie(登录痕迹)，token需要拼接上去
                return "redirect:" + user.getRedirect_url() + "?" + MyConf.TOKEN + "=" + sso_token;
            }
        }
        model.addAttribute("redirect_url", user.getRedirect_url());
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(User user, HttpServletResponse response){
        if(!StringUtils.isEmpty(user.getUsername()) && !StringUtils.isEmpty(user.getPassword())
                && "admin".equals(user.getUsername()) && "admin".equals(user.getPassword())){
            // 登录成功跳转 跳回之前的页面
            String uuid = UUID.randomUUID().toString().replace("-", "");
            Cookie cookie = new Cookie(MyConf.COOKIE_SSO_TOKEN, uuid);
            response.addCookie(cookie);
            this.stringRedisTemplate.opsForValue().set(uuid, "admin", 30, TimeUnit.MINUTES);
            return "redirect:" + user.getRedirect_url() + "?" + MyConf.TOKEN + "=" + uuid;
        }
        // 登录失败
        return "login";
    }

}
