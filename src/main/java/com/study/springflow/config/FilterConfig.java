package com.study.springflow.config;

import com.study.springflow.filter.LoggingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    /**
     * ✅ 서블릿 레벨 요청 필터 등록
     * - DispatcherServlet 이전 단계에서 동작
     * - 요청 URI, IP, 헤더 로깅 등 사전 처리에 유용
     * - 필터는 순서(order)에 따라 다단계로 설정 가능
     */
    @Bean
    public FilterRegistrationBean<Filter> loggingFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingFilter());
        registration.addUrlPatterns("/*");                // 전체 경로에 적용
        registration.setOrder(1);                         // 실행 순서
        registration.setName("LoggingFilter");
        registration.setEnabled(true);                    // 필터 활성화 여부

        /**
         * 🔧 추가 가능 설정:
         * - 특정 경로만 필터 적용: registration.addUrlPatterns("/api/*");
         * - 필터 비활성화: registration.setEnabled(false);
         * - 여러 필터 체인 구성 시 order 조절
         */

        return registration;
    }

    // 여기에 다른 필터도 추가 가능
    // 예: 인증 필터, CORS 필터 등
}
