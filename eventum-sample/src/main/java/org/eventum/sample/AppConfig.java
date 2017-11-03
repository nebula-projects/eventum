/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eventum.sample;
import org.eventum.core.EventStore;
import org.eventum.core.Eventum;
import org.eventum.spring.JdbcEventStore;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@ComponentScan({"org.eventum.sample", "org.eventum.sample.repository"})
@MapperScan("org.eventum.sample.repository")
public class AppConfig {

    @Autowired
    private Eventum eventum;

    @Autowired
    private OrderCreatedEventHandler orderCreatedEventHandler;

    @Autowired
    private PaymentEventHandler paymentEventHandler;

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setMaxActive(5);
        dataSource.setMaxIdle(5);
        dataSource.setInitialSize(5);
        dataSource.setUrl("jdbc:mysql://localhost:3306/eventum_sample");
        dataSource.setUsername("eventum");
        dataSource.setPassword("eventum");
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");

        return dataSource;
    }

//    @Bean
//    public JdbcTemplate jdbcTemplate(DataSource dataSource){
//        return new JdbcTemplate(dataSource);
//    }
//
//    @Bean
//    public PlatformTransactionManager dataSourceTransactionManager(DataSource dataSource){
//        return new DataSourceTransactionManager(dataSource);
//    }
//
//    @Bean
//    public TransactionTemplate transactionTemplate(PlatformTransactionManager dataSourceTransactionManager){
//        return new TransactionTemplate(dataSourceTransactionManager);
//    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
        sessionFactory.setTypeAliasesPackage("org.eventum.sample.domain");
        return sessionFactory.getObject();
    }

//    @Bean
//    public EventConsumer eventConsumer(OrderCreatedEventHandler orderCreatedEventHandler, PaymentEventHandler paymentEventHandler){
//        EventConsumer eventConsumer = new EventConsumer();
//        return eventConsumer.registerHandler(orderCreatedEventHandler).registerHandler(paymentEventHandler);
//    }

    @Bean
    public EventStore eventStore(JdbcTemplate jdbcTemplate){
        return new JdbcEventStore(jdbcTemplate);
    }

    @Bean
    public Eventum eventum(EventStore eventStore){
        return new Eventum(eventStore);
    }

    @PostConstruct
    public void registerEventHandlers() {
        eventum.registerEventHandler(orderCreatedEventHandler).registerEventHandler(paymentEventHandler);
    }
}
