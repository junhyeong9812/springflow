package com.study.springflow.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(1) // 다른 어드바이스보다 먼저 실행
public class TransactionLogAspect {

    /**
     * 트랜잭션 로깅 관점(Aspect)
     * - @Transactional이 적용된 메서드의 트랜잭션 시작/종료/롤백 로깅
     * - AOP와 트랜잭션의 연동 방식 이해를 위한 예시
     */
    @Around("execution(* com.study.springflow.service..*(..)) && @annotation(transactional)")
    public Object logTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        boolean readOnly = transactional.readOnly();

        System.out.println("\n=== 트랜잭션 시작 ===");
        System.out.println("클래스: " + className);
        System.out.println("메서드: " + methodName);
        System.out.println("읽기전용: " + readOnly);
        System.out.println("전파속성: " + transactional.propagation());

        long startTime = System.currentTimeMillis();

        try {
            // 메서드 실행
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            System.out.println("=== 트랜잭션 커밋 ===");
            System.out.println("실행시간: " + (endTime - startTime) + "ms");

            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            System.out.println("=== 트랜잭션 롤백 ===");
            System.out.println("예외: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.out.println("실행시간: " + (endTime - startTime) + "ms");

            throw e;
        }
    }
}