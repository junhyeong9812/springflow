package com.study.springflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopConfig {

    /**
     * ✅ AOP를 활성화하기 위한 설정
     * - Spring은 기본적으로 Proxy 기반 AOP를 사용
     * - proxyTargetClass = true → CGLIB(클래스 기반) 프록시
     * - false일 경우 JDK 동적 프록시(인터페이스 기반)
     *
     * 이 설정이 없으면 @Aspect 클래스가 작동하지 않음
     */
}
