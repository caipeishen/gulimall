package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;
        // 1.准备检索请求
        SearchRequest searchRequest = this.buildSearchRequest(param);
        try {
            // 2.执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, COMMON_OPTIONS);
            // 3.分析响应数据
            result = this.buildSearchResult(response, param);
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

        TermsAggregationBuilder cate_agg_name = AggregationBuilders.terms("cate_name_agg").field("catalogName").size(1);
        
        cate_agg.subAggregation(cate_agg_name);

        sourceBuilder.aggregation(cate_agg);


        // 5.2.品类聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId");

        TermsAggregationBuilder brand_agg_img = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        TermsAggregationBuilder brand_agg_name = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);

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
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();

        // 1.返回的所有查询到的商品
        List<SkuEsModel> esModels = new ArrayList<>();
        if(hits.getHits() != null &&  hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                // ES中检索得到的对象
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(StringUtils.isNotBlank(param.getKeyword())){
                    // 高亮属性
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highlightFields = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(highlightFields);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);


        // 2.当前商品所有涉及到的分类信息
        List<SearchResult.CatalogVo> catalogList = new ArrayList<>();
        ParsedLongTerms cate_agg = response.getAggregations().get("cate_agg");
        for (Terms.Bucket bucket : cate_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());

            String cate_agg_name = ((ParsedStringTerms) bucket.getAggregations().get("cate_name_agg")).getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(cate_agg_name);

            catalogList.add(catalogVo);
        }
        result.setCatalogs(catalogList);


        // 3.当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVoList = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> buckets = brand_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());

            String brand_img = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brand_img);

            String brand_name = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brand_name);

            brandVoList.add(brandVo);
        }
        result.setBrands(brandVoList);


        // 4.当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrList = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_nested_agg = attr_agg.getAggregations().get("attr_nested_agg");
        for (Terms.Bucket bucket : attr_nested_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());

            String attrName = ((ParsedStringTerms)bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                return item.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValue);

            attrList.add(attrVo);
        }
        result.setAttrs(attrList);


        // ================以上信息从聚合信息中获取
        // 5.分页信息-页码
        result.setPageNum(param.getPageNum());

        // 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        // 总页码：计算得到
        int totalPages = (int) (total%EsConstant.PRODUCT_PASIZE == 0 ? (total / EsConstant.PRODUCT_PASIZE) : (total / EsConstant.PRODUCT_PASIZE + 1));
        result.setTotalPages(totalPages);

        // 设置导航页
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for (int i = 1;i <= totalPages; i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6.构建面包屑导航功能
        if(param.getAttrs() != null){
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.getAttrsInfo(Long.parseLong(s[0]));
                // 将已选择的请求参数添加进去 前端页面进行排除
                result.getAttrIds().add(Long.parseLong(s[0]));
                if(r.getCode() == 0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>(){});
                    navVo.setName(data.getAttrName());
                }else{
                    // 失败了就拿id作为名字
                    navVo.setName(s[0]);
                }
                // 拿到所有查询条件 替换查询条件
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }

        // 品牌、分类
        if(param.getBrandId() != null && param.getBrandId().size() > 0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setName("品牌");
            R r = productFeignService.brandInfo(param.getBrandId());
            if(r.getCode() == 0){
                // TODO 这里转换有点问题 不想处理了 品牌名称两个实体类名字不同 所以展示为null
                List<BrandVo> brand = r.getData("data", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                // 替换所有品牌ID
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        log.info("构建结果：" + result);

        return result;
    }

    /**
     * 替换字符
     * key ：需要替换的key
     */
    private String replaceQueryString(SearchParam Param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value,"UTF-8");
            // 浏览器对空格的编码和java的不一样
            encode = encode.replace("+","%20");
            encode = encode.replace("%28", "(").replace("%29",")");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Param.get_queryString().replace("&" + key + "=" + encode, "");
    }

}
