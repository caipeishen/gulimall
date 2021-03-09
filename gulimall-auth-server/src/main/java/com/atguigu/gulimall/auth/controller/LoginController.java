package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.consant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/8 20:17
 * @Description: 认证服务
 **/
@Slf4j
@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @ResponseBody
    @GetMapping("/sms/snedcode")
    public R sendCode(@RequestParam("phone") String phone){

        // TODO 接口防刷
        String redisCode = this.stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(null != redisCode && redisCode.length() > 0){
            long CuuTime = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - CuuTime < 60 * 1000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 6);
        String redis_code = code + "_" + System.currentTimeMillis();
        // 缓存验证码
        this.stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redis_code , 10, TimeUnit.MINUTES);
        try {
            return this.thirdPartFeignService.sendCode(phone, code);
        } catch (Exception e) {
            log.warn("远程调用不知名错误 [无需解决]");
        }
        return R.ok();
    }

}
