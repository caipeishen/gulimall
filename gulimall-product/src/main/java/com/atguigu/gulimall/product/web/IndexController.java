package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @Author: Cai Peishen
 * @Date: 2021/2/19 15:36
 * @Description:
 **/
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;
    
    @RequestMapping({"/", "index", "/index.html"})
    public String indexPage(Model model) {
        // 获取一级分类所有缓存
        List<CategoryEntity> categorys = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categorys);
        return "index";
    }

    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }
    
    /**
     * RLock锁有看门狗机制 会自动帮我们续期，默认30s自动过期
     * lock.lock(10,TimeUnit.SECONDS); 设置过期时间不会自动续期，同时锁的时间一定要大于业务的时间 否则会出现没有锁住（当前业务没有执行完，锁自动过期了，并发请求就会出现没有锁住）
     * <p>
     * 1.如果我们传递了锁的超时时间就给redis发送超时脚本 默认超时时间就是我们指定的
     * 2.如果我们未指定，就使用 30 * 1000 [LockWatchdogTimeout]
     *   只要占锁成功 就会启动一个定时任务 任务就是重新给锁设置过期时间 这个时间还是 [LockWatchdogTimeout] 的时间 1/3 看门狗的时间续期一次 续成满时间
     *   最佳实战：lock.lock(30,TimeUnit.SECONDS);省掉了整个续期操作，超30秒肯定数据库或者代码有问题
     */
    @ResponseBody
    @RequestMapping("/index/hello")
    public String hello() {
        RLock lock = redissonClient.getLock("my-lock");
        // 阻塞式等待
        lock.lock();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return "hello";
    }

}
