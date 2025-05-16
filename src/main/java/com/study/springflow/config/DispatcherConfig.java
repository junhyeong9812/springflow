package com.study.springflow.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class DispatcherConfig {

    /**
     * ✅ DispatcherServlet 수동 등록
     * - Spring Boot는 기본적으로 "/" 경로에 자동 등록
     * - 여기서는 명시적으로 수동 등록하여 흐름 제어 가능
     */
    @Bean
    public ServletRegistrationBean<DispatcherServlet> dispatcherServlet(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/");
        registration.setName("dispatcherServlet");
        registration.setLoadOnStartup(1);

        /**
         * 🔧 추가 설정:
         * - registration.addUrlMappings("/api/*") → 다중 서블릿 경로 분기 가능
         * - registration.setInitParameters(...) → 서블릿 초기 설정 가능
         * - DispatcherServlet의 Multipart 설정 분리 관리도 가능
         */

        return registration;
    }
}
