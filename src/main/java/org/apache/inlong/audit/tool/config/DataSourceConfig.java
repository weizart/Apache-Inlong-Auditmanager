/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.audit.tool.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "org.apache.inlong.manager.dao.mapper", sqlSessionFactoryRef = "managerSqlSessionFactory")
@MapperScan(basePackages = "org.apache.inlong.audit.tool.dao.mapper", sqlSessionFactoryRef = "auditSqlSessionFactory")
public class DataSourceConfig {

    @Bean(name = "managerDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource managerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "managerSqlSessionFactory")
    @Primary
    public SqlSessionFactory managerSqlSessionFactory(@Qualifier("managerDataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "managerTransactionManager")
    @Primary
    public DataSourceTransactionManager managerTransactionManager(
            @Qualifier("managerDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "auditDataSource")
    public DataSource auditDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://127.0.0.1:3306/apache_inlong_audit?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false&allowMultiQueries=true")
                .username("root")
                .password("inlong")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "auditSqlSessionFactory")
    public SqlSessionFactory auditSqlSessionFactory(@Qualifier("auditDataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "auditTransactionManager")
    public DataSourceTransactionManager auditTransactionManager(@Qualifier("auditDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}