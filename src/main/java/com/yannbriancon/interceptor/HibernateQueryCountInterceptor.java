package com.yannbriancon.interceptor;

import org.hibernate.EmptyInterceptor;
import org.springframework.stereotype.Component;

@Component
public class HibernateQueryCountInterceptor extends EmptyInterceptor {

    private transient ThreadLocal<Long> queryCount = new ThreadLocal<>();

    public void startCounter() {
        queryCount.set(0L);
    }

    public Long getQueryCount() {
        return queryCount.get();
    }

    public void removeCounter() {
        queryCount.remove();
    }

    @Override
    public String onPrepareStatement(String sql) {
        Long count = queryCount.get();
        if (count != null) {
            queryCount.set(count + 1);
        }
        return super.onPrepareStatement(sql);
    }
}
