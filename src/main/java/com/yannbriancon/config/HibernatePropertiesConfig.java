package com.yannbriancon.config;

import com.yannbriancon.interceptor.HibernateQueryInterceptor;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan(basePackages = {"com.yannbriancon"})
class HibernatePropertiesConfig implements HibernatePropertiesCustomizer {

    private HibernateQueryInterceptor hibernateQueryInterceptor;

    public HibernatePropertiesConfig(HibernateQueryInterceptor hibernateQueryInterceptor) {
        this.hibernateQueryInterceptor = hibernateQueryInterceptor;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.session_factory.interceptor", hibernateQueryInterceptor);
    }
}
