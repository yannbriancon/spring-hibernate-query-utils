package com.yannbriancon.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@ComponentScan(basePackages = {"com.yannbriancon"})
public class HibernateQueryOnRequestResetInterceptor implements HandlerInterceptor {

	@Autowired
	HibernateQueryInterceptor hibernateQueryInterceptor;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// Reset query detection state on each new request
		hibernateQueryInterceptor.resetNPlusOneQueryDetectionState();
		return true;
	}
}
