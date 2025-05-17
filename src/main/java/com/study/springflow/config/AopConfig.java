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
     *
     * 🔍 추가 활용 옵션:
     * 1. exposeProxy 옵션:
     *    @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
     *    - AopContext.currentProxy()를 통해 현재 프록시에 접근 가능
     *    - 자기 호출 문제(self-invocation) 해결에 유용
     *    - 같은 빈 내에서 다른 @Transactional 메서드 호출 시 트랜잭션 전파에 필요
     *    - 예시:
     *      public void method1() {
     *          // 프록시를 통한 호출로 AOP 적용됨
     *          ((MyService) AopContext.currentProxy()).method2();
     *      }
     *
     * 2. 수동 Advisor 등록:
     *    @Bean
     *    public DefaultPointcutAdvisor loggingAdvisor() {
     *        // Pointcut 정의 - 어떤 메서드에 적용할지
     *        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
     *        pointcut.setExpression("execution(* com.study.springflow.service.*.*(..))");
     *
     *        // Advice 정의 - 무엇을 적용할지
     *        MethodInterceptor advice = invocation -> {
     *            System.out.println("Before: " + invocation.getMethod().getName());
     *            Object result = invocation.proceed();
     *            System.out.println("After: " + invocation.getMethod().getName());
     *            return result;
     *        };
     *
     *        // Advisor = Pointcut + Advice
     *        return new DefaultPointcutAdvisor(pointcut, advice);
     *    }
     *
     * 3. 트랜잭션 AOP 설정 예시:
     *    @Bean
     *    public TransactionInterceptor transactionInterceptor(PlatformTransactionManager transactionManager) {
     *        Properties txAttributes = new Properties();
     *        txAttributes.setProperty("get*", "PROPAGATION_REQUIRED,readOnly");
     *        txAttributes.setProperty("find*", "PROPAGATION_REQUIRED,readOnly");
     *        txAttributes.setProperty("*", "PROPAGATION_REQUIRED");
     *
     *        TransactionInterceptor txAdvice = new TransactionInterceptor();
     *        txAdvice.setTransactionManager(transactionManager);
     *        txAdvice.setTransactionAttributes(txAttributes);
     *        return txAdvice;
     *    }
     *
     *    @Bean
     *    public Advisor transactionAdvisor(TransactionInterceptor txAdvice) {
     *        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
     *        pointcut.setExpression("execution(* com.study.springflow.service.*.*(..))");
     *        return new DefaultPointcutAdvisor(pointcut, txAdvice);
     *    }
     *
     * 4. 다양한 weaving 방식:
     *    - Compile-time weaving: AspectJ 컴파일러를 사용하여 컴파일 시점에 aspect 코드 삽입
     *      (ajc 컴파일러 + 메이븐/그래들 플러그인 필요)
     *    - Load-time weaving: 클래스 로딩 시점에 바이트코드 변환
     *      spring.aop.auto=false
     *      spring.aop.proxy-target-class=false
     *      context:load-time-weaver 설정 추가
     *
     * 🔧 흐름 테스트 방법:
     * 1. AOP 프록시 확인:
     *    @Autowired
     *    private MyService myService;
     *
     *    @PostConstruct
     *    public void checkProxy() {
     *        System.out.println("Actual class: " + myService.getClass().getName());
     *        // 출력: com.study.springflow.service.MyServiceImpl$$EnhancerBySpringCGLIB$$12345
     *    }
     *
     * 2. 어드바이스 실행 순서 확인:
     *    @Around("execution(* com.study.springflow.service.*.*(..))")
     *    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
     *        long start = System.currentTimeMillis();
     *        Object proceed = joinPoint.proceed();
     *        long time = System.currentTimeMillis() - start;
     *        System.out.println(joinPoint.getSignature() + " executed in " + time + "ms");
     *        return proceed;
     *    }
     *
     * 3. 다중 어드바이스 실행 순서(@Order 어노테이션으로 제어):
     *    @Aspect
     *    @Order(1) // 낮은 숫자가 먼저 실행
     *    public class SecurityAspect { ... }
     *
     *    @Aspect
     *    @Order(2)
     *    public class LoggingAspect { ... }
     */
}