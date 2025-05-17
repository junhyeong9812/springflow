package com.study.springflow.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    /**
     * AOP를 활용한 로깅
     * - 컨트롤러 메서드 실행 전/후 로깅
     * - 메서드 실행 시간 측정
     * - 포인트컷 표현식을 통해 어떤 메서드에 적용할지 지정
     */
    @Before("execution(* com.study.springflow.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("[LogAspect] 컨트롤러 메서드 실행 전: " +
                joinPoint.getSignature().getDeclaringTypeName() + "." +
                joinPoint.getSignature().getName());
    }

    @Around("execution(* com.study.springflow.controller.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();
        System.out.println("[LogAspect] " + joinPoint.getSignature().getName() +
                " 메서드 실행 시간: " + (end - start) + "ms");

        return result;
    }
}