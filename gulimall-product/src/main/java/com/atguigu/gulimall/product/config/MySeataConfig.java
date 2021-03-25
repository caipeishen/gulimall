package com.atguigu.gulimall.product.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;


/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 11:02
 * @Description: seata数据源配置(分布式事务)
 */
@Configuration
public class MySeataConfig {

	@Bean
	public DataSource dataSource(DataSourceProperties dataSourceProperties){
		HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		if(StringUtils.hasText(dataSourceProperties.getName())){
			dataSource.setPoolName(dataSourceProperties.getName());
		}
		return new DataSourceProxy(dataSource);
	}
	
}
