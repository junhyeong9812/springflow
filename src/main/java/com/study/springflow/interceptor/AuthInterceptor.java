package com.study.springflow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    // ✅ 요청 전 처리
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("[AuthInterceptor] 요청 URL: " + request.getRequestURI());
        return true; // false일 경우 컨트롤러로 요청이 전달되지 않음
    }

    // 필요 시 postHandle, afterCompletion도 구현 가능
}
