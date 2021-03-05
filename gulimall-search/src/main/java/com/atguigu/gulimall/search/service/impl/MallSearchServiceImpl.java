package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;

import static com.atguigu.gulimall.search.config.GulimallElasticSearchConfig.COMMON_OPTIONS;

/**
 * @Author: Cai Peishen
 * @Date: 2021/2/28 14:46
 * @Description: 检索请求
 **/
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult result = null;
        // 1.准备检索请求
        SearchRequest searchRequest = this.buildSearchRequest(searchParam);
        try {
            // 2.执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, COMMON_OPTIONS);
            // 3.分析响应数据
            result = this.buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求
     * 模糊匹配，过滤（按照分类，品牌，属性，库存，价格区间），排序，分页，高亮，聚合分析(分类、品牌、属性、属性值)
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        // 构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 1.过滤
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                // 检索的id  属性检索用的值
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                // 构建一个嵌入式Query 每一个必须都得生成嵌入的 nested 查询
                NestedQueryBuilder attrsQuery = QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None);
                boolQuery.filter(attrsQuery);
            }
        }
        if(param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",param.getHasStock() == 1));
        }
        if(StringUtils.isNotBlank(param.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length == 2){
                // 有三个值 就是区间
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if(s.length == 1){
                // 单值情况
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }


        // 把以前所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);


        // 2.排序
        if(StringUtils.isNotBlank(param.getSort())){
            String sort = param.getSort();
            // sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }


        // 3.分页 pageSize ： 5
        sourceBuilder.from((param.getPageNum()-1) * EsConstant.PRODUCT_PASIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PASIZE);


        // 4.高亮
        if(StringUtils.isNotBlank(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }


        // 5.聚合

        // 5.1.分类聚合
        TermsAggregationBuilder cate_agg = AggregationBuilders.terms("cate_agg").field("catalogId").size(20);

        TermsAggregationBuilder cate_agg_name = AggregationBuilders.terms("cate_agg_name").field("catalogName").size(1);
        
        cate_agg.subAggregation(cate_agg_name);

        sourceBuilder.aggregation(cate_agg);


        // 5.2.品类聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId");

        TermsAggregationBuilder brand_agg_img = AggregationBuilders.terms("brand_agg_img").field("brandImg").size(1);
        TermsAggregationBuilder brand_agg_name = AggregationBuilders.terms("brand_agg_name").field("brandName").size(1);

        brand_agg.subAggregation(brand_agg_img);
        brand_agg.subAggregation(brand_agg_name);

        sourceBuilder.aggregation(brand_agg);
        
        
        // 5.3.属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_nested_agg = AggregationBuilders.terms("attr_nested_agg").field("attrs.attrId");

        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);

        attr_nested_agg.subAggregation(attr_name_agg);
        attr_nested_agg.subAggregation(attr_value_agg);

        attr_agg.subAggregation(attr_nested_agg);

        sourceBuilder.aggregation(attr_agg);


        log.info("构建DSL语句：" + sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }

    /**
     * 封装返回结果
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response) {
        SearchResult result = new SearchResult();
        return result;
    }

}
