package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private StringRedisTemplate stringRedisTemplate;
    
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
    
    /**
     * 保证一定能读到最新数据,修改期间，写锁是一个排他锁（互斥锁、独享锁?。读锁是一个共享锁写锁没释放读就必须等待
     * 读 + 读：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
     * 写 + 读：等待写锁释放
     * 写 + 写：阻塞方式
     * 读 + 写：有读锁。写也需要等待。只要有写的存在，都必须等待
     */
    
    /**
     * 读写锁
     */
    @GetMapping("/index/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(3000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }
    
    
    /**
     * 读写锁
     */
    @GetMapping("/index/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s = "";
        rLock.lock();
        try {
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }
    
    /**
     * 闭锁 只有设定的人全通过才关门
     */
    @ResponseBody
    @GetMapping("/index/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        // 设置这里有5个人
        door.trySetCount(5);
        door.await();
        return "5个人全部通过了...";
    }
    
    @ResponseBody
    @GetMapping("/index/go/{id}")
    public String go(@PathVariable("id") Long id) throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        // 每访问一次相当于出去一个人
        door.countDown();
        return id + "走了";
    }
    
    /**
     * 尝试获取车位 [信号量]
     * 信号量:也可以用作限流
     */
    @ResponseBody
    @GetMapping("/index/park")
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        //park.acquire();
        boolean acquire = park.tryAcquire();// 尝试获取信号量，有则true，无则false
        return "获取车位 =>" + acquire;
    }
    
    /**
     * 尝试获取车位
     */
    @ResponseBody
    @GetMapping("/index/park/go")
    public String goPark() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();
        return "ok => 车位+1";
    }
    
}
