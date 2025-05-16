package com.study.springflow.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("[LoggingFilter] ▶️ 필터 초기화 완료");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String clientIp = httpRequest.getRemoteAddr();

        log.info("[LoggingFilter] ▶️ 요청: [{}] {} from {}", method, uri, clientIp);

        chain.doFilter(request, response);

        log.info("[LoggingFilter] ⏹️ 응답 완료: [{}] {}", method, uri);
    }

    @Override
    public void destroy() {
        log.info("[LoggingFilter] ❌ 필터 종료");
    }
}
