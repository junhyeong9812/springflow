package com.study.springflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class ViewResolverConfig {

    /**
     * ✅ JSP 기반 ViewResolver 설정
     * - Spring Boot는 자동 설정되지만, 여기서는 명시적으로 수동 등록
     * - 예: "hello" → /WEB-INF/views/hello.jsp
     */
    @Bean
    public ViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");    // 뷰 파일 경로
        resolver.setSuffix(".jsp");               // 확장자
        resolver.setOrder(0);                     // 우선순위 (낮을수록 우선)

        /**
         * 🔧 추가 설정:
         * - resolver.setExposeContextBeansAsAttributes(true); → 컨텍스트 내 모든 Bean 모델 노출
         * - resolver.setViewNames("*.jsp"); → 특정 뷰 이름만 매핑
         */

        return resolver;
    }
}
