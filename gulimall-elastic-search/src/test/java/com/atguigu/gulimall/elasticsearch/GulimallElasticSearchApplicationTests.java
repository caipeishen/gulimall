package com.atguigu.gulimall.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.elasticsearch.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallElasticSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("username", "张三", "age", 18, "gender", "男");
        User user = new User();
        user.setAge(18);
        user.setGenger("男");
        user.setUsername("张三");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse indexResponse = client.index(indexRequest, GulimallElasticSearchConfig.COMNON_OPTIONS);
        System.out.println(indexResponse.toString());
    }


    @Data
    class User {
        private String username;
        private String genger;
        private Integer age;
    }

    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
