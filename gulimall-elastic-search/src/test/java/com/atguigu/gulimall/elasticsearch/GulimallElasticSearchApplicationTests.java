package com.atguigu.gulimall.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.elasticsearch.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallElasticSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void searchData() throws IOException {
        // 1.创建检索请求
        SearchRequest searchRequest = new SearchRequest("bank");

        // 2.指定DSL
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 2.1.检索条件
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("address", "mill");
        sourceBuilder.query(matchQuery);

        // 2.2.聚合条件
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age");
        sourceBuilder.aggregation(ageAgg);

        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("DSL语句:" + sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        // 3.执行检索，返回检索数据
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMNON_OPTIONS);// RequestOptions.DEFAULT

        // 4.分析结果
        SearchHits hits = searchResponse.getHits();
        long total = hits.getTotalHits().value;
        for (SearchHit hit : hits.getHits()) {
            Accout accout = JSON.parseObject(hit.getSourceAsString(), Accout.class);
            System.out.println("Accout:" + accout.toString());
        }

        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAggResult = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAggResult.getBuckets()) {
            System.out.println("ageAgg-key-年龄:" + bucket.getKey());
            System.out.println("ageAgg-docCount-个数:" + bucket.getDocCount());
        }

        Avg balanceAvgResult = aggregations.get("balanceAvg");
        System.out.println("balanceAvg-平均薪资:" + balanceAvgResult.getValue());

        System.out.println(searchResponse.toString());
    }




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

    @Test
    void contextLoads() {
        System.out.println(client);
    }

    @Data
    class User {
        private String username;
        private String genger;
        private Integer age;
    }

    @Data
    static class Accout {
        private Long accountNumber;
        private Long balance;
        private String firstname;
        private String lastname;
        private Integer age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

}
