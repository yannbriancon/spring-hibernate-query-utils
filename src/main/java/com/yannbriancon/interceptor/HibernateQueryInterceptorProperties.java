package com.yannbriancon.interceptor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@ConfigurationProperties("hibernate.query.interceptor")
public class HibernateQueryInterceptorProperties implements Serializable {
    enum ErrorLevel {
        INFO,
        WARN,
        ERROR,
        EXCEPTION
    }

    /**
     * Error level for the N+1 query detection.
     */
    private ErrorLevel errorLevel = ErrorLevel.ERROR;

    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = ErrorLevel.valueOf(errorLevel);
    }
}
