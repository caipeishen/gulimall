package com.atguigu.gulimall.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1.导入配置
 * 2.编写配置，注入到容器中
 * 3.参照API https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 *
 * @Author: Cai Peishen
 * @Date: 2021/2/15 19:05
 * @Description: ElasticSearch配置
 **/
@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//            builder.addHeader("Authorization", "Bearer" + TOKEN);
//            builder.setHttpAsyncResponseConsumerFactory(
//                    new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30*1024*1024*1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestclient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.181.130", 9200, "http")));
        return client;
    }

}