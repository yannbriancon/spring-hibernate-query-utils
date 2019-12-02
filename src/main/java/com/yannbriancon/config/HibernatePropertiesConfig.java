package com.yannbriancon.config;

import com.yannbriancon.HibernateQueryCountInterceptor;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HibernatePropertiesConfig implements HibernatePropertiesCustomizer {

    private HibernateQueryCountInterceptor hibernateQueryCountInterceptor;

    public HibernatePropertiesConfig(HibernateQueryCountInterceptor hibernateQueryCountInterceptor) {
        this.hibernateQueryCountInterceptor = hibernateQueryCountInterceptor;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.session_factory.interceptor", hibernateQueryCountInterceptor);
    }
}
