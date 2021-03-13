package com.atguigu.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在@Validated生效；
 *
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
 *      @Documented
 *      @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
 *      @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
 *      @Retention(RUNTIME)
 *      public @interface ListValue { }
 *
 *
 * 8、整合SpringCache简化缓存开发
 *      1)、引入依赖
 *              spring-boot-starter-cache、 spring-boot-starter-data-redis
 *      2) 、写配置
 *              2.1、自动配置了哪些
 *                  CacheAutoConfiguration会导入RedisCacheConfiguration;自动配好了缓存管理器RedisCacheManager
 *              2.2、配置使用redis作为缓存
 *                  spring.cache.type=redis
 *      3)、测试使用缓存
 *              @Cacheable:Triggers cache population.:触发将数据保存到缓存的操作
 *              @CacheEvict:Triggers cache eviction.:触发将数据从缓存删除的操作
 *              @CachePut: Updates the cache without interfering with the method execution.:不影响方法执行更
 *              @Caching:Regroups multiple cache operations to be applied on a method.:组合以上多个操作
 *              @CacheConfig: Shares some common cache-related settings at class-Level.:在类级别共享缓存的福
 *              3.1、开启缓存功能
 *              3.2、只需要使用注解就可以完成缓存操作
 *
 *      4)、原理:
 *              CacheAutoConfiguration ->RedisCacheConfiguration ->
 *              自动配置了RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定使用什么配置
 *              ->如果RedisCacheConfiguration有就用已有的,没有就用默认配置
 *              ->想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *              ->就会应用到当前RedisCacheManager管理的所有缓存分区中
 *
 *      5）、Spring-Cache的不足;
 *              1)、读模式:
 *                  缓存穿透:查询一个null数据。解决:缓存空数据;ache-null-values=true
 *                  缓存击穿:大量并发进来同时查询一个正好过期的数据。解决:加锁;?默认是无加锁的;sync = true
 *                  缓存雪崩:大量的key同时过期。解决。加随机时间。加上过期时间。: spring.cache.redis.time-to-live
 *              2)、写模式:(缓存与数据库一致)
 *                  1)、读写加锁。
 *                  2)、引入Canal，感知到MysQL的更新去更新数据库
 *                  3)、读多写多,直接去数据库查询就行
 *              总结:
 *                  常规数据（读多写少，即时性，一致性要求不高的数据）﹔完全可以使用spring-cache
 *                  特殊数据:特殊设计
 *              原理:
 *                  CacheManager(RediscacheManager)->cache(RedisCache)->cache负责缓存的读写
 **/
// 开启redis 存储session
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
    
}
