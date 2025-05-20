# SpringFlow 프로젝트

## 프로젝트 개요

**SpringFlow**는 Spring Boot를 기반으로 하지만, 자동 설정에 의존하지 않고 **Spring Framework의 수동 구성 방식**을 직접 재현하면서 **Spring 내부 동작 원리와 요청 처리 흐름(MVC 구조)을 학습**하기 위한 실습용 프로젝트입니다.

Spring의 요청 처리 흐름, 트랜잭션 처리, 예외 처리, DispatcherServlet 등록, AOP 적용 등 다양한 컴포넌트를 **직접 설정하고 흐름을 눈으로 확인하는 실험형 프로젝트**입니다.

이 프로젝트의 최종 목적은 Spring Boot의 편의성에 가려 잘 보이지 않던 **Spring의 핵심 구조와 시동 흐름**을 명확하게 이해하고, 실무 설계 시 더 **유연하고 판단력 있는 개발자**로 성장하는 데 있습니다.

## 목표

* Spring MVC 요청 처리 흐름에 대한 깊은 이해 (DispatcherServlet → Filter → Interceptor → Controller → ViewResolver)
* Spring Boot 자동 설정을 배제하고 수동으로 필요한 Bean 구성
* AOP, 트랜잭션, 글로벌 예외 처리 등 주요 컴포넌트의 동작 시점과 원리 실습
* 로그를 통해 각 컴포넌트의 실행 순서를 명확하게 추적
* "Spring Boot 없이도 작동 가능한 구조"를 이해한 뒤, 왜 Spring Boot가 필요한지도 스스로 깨달을 수 있도록 구성

## 기술 스택

* Java 17
* Spring Boot 3.4.5 *(단, 자동 설정을 최소화하여 Spring Framework처럼 사용)*
* Gradle (Groovy DSL)
* Lombok (코드 간결화)
* (선택) H2 Database / JPA / Thymeleaf (추가 실험 시)

## 주요 구성 및 설명

### 1. `config`

스프링 부트의 자동 설정을 우회하고, 명시적으로 DispatcherServlet, Filter, Interceptor, ViewResolver, AOP 설정 등을 등록하는 설정 클래스를 포함합니다.

#### AopConfig

* `@EnableAspectJAutoProxy`를 사용하여 AOP 설정을 수동으로 명시합니다.

  * **proxyTargetClass=true 설정**:
    * CGLIB 기반 프록시 생성을 활성화합니다.
    * CGLIB(Code Generation Library)는 클래스 기반 프록시로, 타겟 클래스를 상속받아 서브클래스를 생성합니다.
    * JDK 동적 프록시(인터페이스 기반)와 달리 인터페이스가 없는 클래스에도 AOP를 적용할 수 있습니다.
    * final 클래스나 메서드에는 적용할 수 없고, 기본 생성자가 필요합니다.

  * **exposeProxy=true 옵션**:
    * AOP 프록시를 현재 스레드의 ThreadLocal에 노출시키는 설정입니다.
    * `AopContext.currentProxy()`를 통해 현재 실행 중인 메서드의 AOP 프록시에 접근할 수 있게 합니다.
    * Self-Invocation(자기 호출) 문제를 해결할 때 유용합니다.

  * **Self-Invocation 문제**:
    * 같은 클래스 내에서 메서드 A가 메서드 B를 호출할 때, B에 적용된 어드바이스(예: @Transactional)가 작동하지 않는 문제입니다.
    * 내부 호출은 프록시를 통하지 않고 직접 대상 객체의 메서드를 호출하기 때문에 발생합니다.
    * `AopContext.currentProxy()`를 사용하거나, 서비스 자신을 주입받아 프록시를 통해 호출하는 방식으로 해결합니다.

  * **수동 Advisor 등록**:
    * Advisor는 Pointcut(어디에 적용할지)과 Advice(무엇을 적용할지)를 결합한 객체입니다.
    * 어노테이션 대신 프로그래밍 방식으로 AOP를 설정할 때 사용합니다.
    * 세밀한 제어가 필요하거나, 런타임에 AOP 설정을 변경해야 할 때 유용합니다.
    * 예: `DefaultPointcutAdvisor`를 빈으로 등록하여 특정 패턴의 메서드에 로깅 또는 트랜잭션 기능을 적용

  **코드 예시 및 추가 설정 옵션:**
    ```java
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    public class AopConfig {
        // exposeProxy=true 설정 추가
        // @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
        
        // 수동 Advisor 등록 예시
        @Bean
        public DefaultPointcutAdvisor loggingAdvisor() {
            AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
            pointcut.setExpression("execution(* com.study.springflow.service.*.*(..))");
            
            MethodInterceptor advice = invocation -> {
                System.out.println("Before: " + invocation.getMethod().getName());
                Object result = invocation.proceed();
                System.out.println("After: " + invocation.getMethod().getName());
                return result;
            };
            
            return new DefaultPointcutAdvisor(pointcut, advice);
        }
        
        // 트랜잭션 AOP 설정 예시
        @Bean
        public TransactionInterceptor transactionInterceptor(PlatformTransactionManager transactionManager) {
            Properties txAttributes = new Properties();
            txAttributes.setProperty("get*", "PROPAGATION_REQUIRED,readOnly");
            txAttributes.setProperty("find*", "PROPAGATION_REQUIRED,readOnly");
            txAttributes.setProperty("*", "PROPAGATION_REQUIRED");
            
            TransactionInterceptor txAdvice = new TransactionInterceptor();
            txAdvice.setTransactionManager(transactionManager);
            txAdvice.setTransactionAttributes(txAttributes);
            return txAdvice;
        }
    }
    ```

#### DispatcherConfig

* `DispatcherServlet`을 명시적으로 수동 등록하여 요청 진입점 흐름을 실험합니다.

  * **DispatcherServlet이란?**:
    * Spring MVC의 핵심 컴포넌트로, Front Controller 패턴을 구현한 클래스입니다.
    * 클라이언트의 모든 요청은 DispatcherServlet이 가장 먼저 받아 처리 흐름을 제어합니다.
    * 내부적으로 HandlerMapping, HandlerAdapter, ViewResolver 등과 협력해 요청을 컨트롤러에 위임하고, 응답을 뷰로 전달합니다.
    * 보통 Spring Boot에서는 자동으로 등록되지만, 본 프로젝트에서는 명시적으로 등록하여 요청 흐름을 관찰합니다.

  **코드 예시 및 추가 설정 옵션:**
    ```java
    @Configuration
    public class DispatcherConfig {
        @Bean
        public ServletRegistrationBean<DispatcherServlet> dispatcherServlet(DispatcherServlet dispatcherServlet) {
            ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/");
            registration.setName("dispatcherServlet");
            registration.setLoadOnStartup(1);
            
            // 추가 설정 옵션:
            // 1. 다중 URL 패턴 설정
            // registration.addUrlMappings("/app/*", "/web/*");
            
            // 2. 서블릿 초기화 파라미터 설정
            // Map<String, String> params = new HashMap<>();
            // params.put("throwExceptionIfNoHandlerFound", "true");
            // registration.setInitParameters(params);
            
            // 3. 멀티파트 설정
            // registration.setMultipartConfig(
            //     new MultipartConfigElement("/tmp/uploads", 5 * 1024 * 1024, 25 * 1024 * 1024, 1 * 1024 * 1024)
            // );
            
            return registration;
        }
        
        // 다중 DispatcherServlet 등록 예시
        @Bean
        public DispatcherServlet apiDispatcherServlet() {
            DispatcherServlet servlet = new DispatcherServlet();
            servlet.setThrowExceptionIfNoHandlerFound(true);
            return servlet;
        }
        
        @Bean
        public ServletRegistrationBean<DispatcherServlet> apiServletRegistration() {
            return new ServletRegistrationBean<>(apiDispatcherServlet(), "/api/*");
        }
    }
    ```
  #### FilterConfig

* `FilterRegistrationBean`을 통해 서블릿 레벨 필터를 등록합니다.

  **코드 예시 및 추가 설정 옵션:**
    ```java
    @Configuration
    public class FilterConfig {
        @Bean
        public FilterRegistrationBean<Filter> loggingFilter() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new LoggingFilter());
            registration.addUrlPatterns("/*");                // 전체 경로에 적용
            registration.setOrder(1);                         // 실행 순서
            registration.setName("LoggingFilter");
            registration.setEnabled(true);                    // 필터 활성화 여부
            
            // 추가 설정 옵션:
            // 1. 특정 경로만 필터 적용
            // registration.addUrlPatterns("/api/*");
            
            // 2. 필터 초기화 파라미터 설정
            // Map<String, String> initParams = new HashMap<>();
            // initParams.put("logLevel", "DEBUG");
            // initParams.put("includePayload", "true");
            // registration.setInitParameters(initParams);
            
            return registration;
        }
        
        // 다중 필터 체인 구성 예시
        @Bean
        public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
            FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
            CharacterEncodingFilter filter = new CharacterEncodingFilter();
            filter.setEncoding("UTF-8");
            filter.setForceEncoding(true);
            registration.setFilter(filter);
            registration.addUrlPatterns("/*");
            registration.setOrder(0); // 가장 먼저 실행
            return registration;
        }
    }
    ```

#### ViewResolverConfig

* `InternalResourceViewResolver`를 직접 등록하여 JSP 뷰를 처리합니다.

  **코드 예시 및 추가 설정 옵션:**
    ```java
    @Configuration
    public class ViewResolverConfig {
        @Bean
        public ViewResolver internalResourceViewResolver() {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/WEB-INF/views/");    // 뷰 파일 경로
            resolver.setSuffix(".jsp");               // 확장자
            resolver.setOrder(0);                     // 우선순위 (낮을수록 우선)
            
            // 추가 설정 옵션:
            // 1. 컨텍스트 내 Bean을 뷰에서 참조 가능하게 설정
            // resolver.setExposeContextBeansAsAttributes(true);
            
            // 2. 특정 뷰 이름 패턴만 처리
            // resolver.setViewNames("jsp*");
            
            return resolver;
        }
        
        // 다중 ViewResolver 설정 예시
        @Bean
        public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager,
                                                          List<ViewResolver> resolvers) {
            ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
            resolver.setContentNegotiationManager(manager);
            resolver.setViewResolvers(resolvers);
            resolver.setOrder(-1); // 최우선 순위
            return resolver;
        }
        
        // JSON View 등록 예시
        @Bean(name = "jsonView")
        public MappingJackson2JsonView jsonView() {
            MappingJackson2JsonView view = new MappingJackson2JsonView();
            view.setPrettyPrint(true);
            return view;
        }
    }
    ```

#### WebMvcConfig

* **WebMvcConfigurer 인터페이스**를 구현하여 다음 설정을 수동으로 구성합니다:

  * **Interceptor**: Spring MVC에서 컨트롤러 실행 전후 로직을 삽입할 수 있는 컴포넌트
  * **CORS**: Cross-Origin Resource Sharing 설정
  * **ViewController**: 단순 뷰 이동을 위한 URL → View 매핑 설정
  * **MessageConverter**: 요청/응답 변환기(Jackson, XML 등)를 확장하거나 교체 가능
  * **HandlerExceptionResolver**: 예외를 뷰 또는 JSON 응답으로 매핑

  **코드 예시 및 추가 설정 옵션:**
    ```java
    @Configuration
    public class WebMvcConfig implements WebMvcConfigurer {
        // 정적 리소스 핸들러 설정
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");
                    
            // 추가 설정 옵션:
            // 1. 리소스 캐싱 설정
            // .setCachePeriod(3600) // 1시간 캐싱
            // .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
            
            // 2. 다중 리소스 위치 설정
            // .addResourceLocations("classpath:/static/", "classpath:/public/");
        }
        
        // CORS 설정
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
                    
            // 추가 설정 옵션:
            // 1. 특정 도메인만 허용
            // .allowedOrigins("https://trusted-client.com")
            
            // 2. 프리플라이트 캐시 설정
            // .maxAge(3600) // 1시간 동안 프리플라이트 결과 캐싱
        }
        
        // 인터셉터 등록
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new AuthInterceptor())
                    .addPathPatterns("/**")
                    .excludePathPatterns("/static/**", "/error")
                    .order(1);
                    
            // 추가 설정 옵션:
            // 1. 다중 인터셉터 등록
            // registry.addInterceptor(new LoggingInterceptor())
            //         .addPathPatterns("/**")
            //         .order(0); // 인증 인터셉터보다 먼저 실행
        }
        
        // 뷰 컨트롤러 등록
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/login").setViewName("login");
            registry.setOrder(0);
            
            // 추가 설정 옵션:
            // 1. 리디렉션 컨트롤러
            // registry.addRedirectViewController("/", "/home");
            
            // 2. 상태 코드 컨트롤러
            // registry.addStatusController("/health", HttpStatus.OK);
        }
        
        // 경로 매치 설정
        @Override
        public void configurePathMatch(PathMatchConfigurer configurer) {
            configurer.setUseTrailingSlashMatch(true);
            configurer.setUseSuffixPatternMatch(false);
            
            // 추가 설정 옵션:
            // 1. 매트릭스 변수 활성화
            // configurer.setRemoveSemicolonContent(false);
            
            // 2. 대소문자 구분 설정
            // configurer.setCaseSensitive(true);
        }
        
        // 컨트롤러 메서드 인자 리졸버 등록
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            // 커스텀 인자 리졸버 등록 예시
            // resolvers.add(new CurrentUserArgumentResolver());
        }
        
        // 예외 처리기 등록
        @Override
        public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
            // 커스텀 예외 리졸버 등록 예시
            // SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
            // Properties mappings = new Properties();
            // mappings.setProperty(IllegalArgumentException.class.getName(), "error/badRequest");
            // resolver.setExceptionMappings(mappings);
            // resolver.setDefaultErrorView("error/default");
            // resolvers.add(resolver);
        }
        
        // 메시지 컨버터 설정
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            // Jackson JSON 컨버터 설정 예시
            // MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            // ObjectMapper objectMapper = jsonConverter.getObjectMapper();
            // objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            // converters.add(jsonConverter);
        }
        
        // 비동기 요청 처리 설정
        @Override
        public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
            // configurer.setDefaultTimeout(30000); // 30초 타임아웃
            // configurer.setTaskExecutor(taskExecutor());
        }
        
        // 컨텐츠 협상 설정
        @Override
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
            // configurer.favorParameter(true) // URL 파라미터 사용: ?format=json
            //          .parameterName("format")
            //          .defaultContentType(MediaType.APPLICATION_JSON);
        }
    }
    ```

### 2. `filter`

Servlet container 레벨에서 동작하며, 요청이 DispatcherServlet에 도달하기 전에 사전 처리 역할을 합니다.

#### LoggingFilter

* `javax.servlet.Filter`를 구현하여 요청 URL, 메서드, 클라이언트 IP 등을 로깅합니다.

  * `init()` / `doFilter()` / `destroy()`를 통해 생명주기 확인
  * 순서 설정으로 다단계 필터링 구현 가능
  * `FilterRegistrationBean`으로 설정

  **코드 예시 및 확장 기능:**
    ```java
    @Slf4j
    public class LoggingFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) {
            log.info("[LoggingFilter] ▶️ 필터 초기화 완료");
            
            // 초기화 파라미터 읽기
            // String logLevel = filterConfig.getInitParameter("logLevel");
            // log.info("설정된 로그 레벨: {}", logLevel);
        }
    
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            String clientIp = httpRequest.getRemoteAddr();
    
            log.info("[LoggingFilter] ▶️ 요청: [{}] {} from {}", method, uri, clientIp);
            
            // 성능 측정 추가
            long startTime = System.currentTimeMillis();
    
            try {
                // 요청 본문 래핑 예시
                // ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
                // chain.doFilter(requestWrapper, response);
                // byte[] content = requestWrapper.getContentAsByteArray();
                // if (content.length > 0) {
                //    log.debug("요청 본문: {}", new String(content));
                // }
                
                chain.doFilter(request, response);
            } finally {
                long endTime = System.currentTimeMillis();
                log.info("[LoggingFilter] ⏹️ 응답 완료: [{}] {} - {}ms", 
                         method, uri, (endTime - startTime));
            }
        }
    
        @Override
        public void destroy() {
            log.info("[LoggingFilter] ❌ 필터 종료");
        }
    }
    ```
  ### 3. `interceptor`

Spring MVC의 HandlerMapping → Controller 진입 전/후를 제어할 수 있는 레벨입니다.

* **정의**: `HandlerInterceptor`를 구현해 요청 흐름을 중간에 가로채고, 사전/사후 로직을 실행
* **AuthInterceptor**: 인증이나 공통 로직 처리에 활용됩니다.

  * `preHandle()`에서 로그인 확인, 권한 체크 가능
  * `postHandle()`에서 로깅, 리다이렉션 처리
  * `afterCompletion()`에서 리소스 정리

  **코드 예시 및 확장 기능:**
    ```java
    public class AuthInterceptor implements HandlerInterceptor {
        // 컨트롤러 실행 전 호출
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            System.out.println("[AuthInterceptor] 요청 URL: " + request.getRequestURI());
            
            // 인증 처리 예시
            // String token = request.getHeader("Authorization");
            // if (token == null || !isValidToken(token)) {
            //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            //     response.getWriter().write("인증이 필요합니다.");
            //     return false; // 컨트롤러 실행 중단
            // }
            
            // 권한 검사 예시 (핸들러가 메서드 핸들러인 경우)
            // if (handler instanceof HandlerMethod) {
            //     HandlerMethod handlerMethod = (HandlerMethod) handler;
            //     AdminOnly adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class);
            //     if (adminOnly != null && !isAdmin(request)) {
            //         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            //         return false;
            //     }
            // }
            
            return true; // true 반환 시 컨트롤러로 진행
        }
        
        // 컨트롤러 실행 후, 뷰 렌더링 전 호출
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, ModelAndView modelAndView) throws Exception {
            // 뷰에 공통 데이터 추가
            // if (modelAndView != null) {
            //     modelAndView.addObject("serverTime", LocalDateTime.now());
            // }
        }
        
        // 뷰 렌더링 후 호출
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                   Object handler, Exception ex) throws Exception {
            // 예외 처리 및 로깅
            // if (ex != null) {
            //     System.err.println("요청 처리 중 오류 발생: " + ex.getMessage());
            // }
            
            // 리소스 정리
            // cleanupResources(request);
        }
    }
    ```

### 4. `aop`

**관심사 분리(Aspect-Oriented Programming)**: 공통 기능(로깅, 트랜잭션, 보안 등)을 핵심 비즈니스 로직과 분리하여 재사용성을 높이는 기법

* **용어 설명**:

  * **Aspect**: 횡단 관심사의 모음 (예: 로깅)
  * **Join Point**: Advice가 적용될 수 있는 지점 (메서드 호출 등)
  * **Advice**: 실제 실행될 로직 (Before, After, Around 등)
  * **Pointcut**: 어떤 JoinPoint에 Advice를 적용할지 정의하는 표현식

* **LogAspect**: `@Aspect`, `@Around`, `@Before`, `@AfterReturning` 등으로 Service 계층 로깅 구현 가능

* `@EnableAspectJAutoProxy`로 활성화

* **트랜잭션과 AOP의 관계**:

  * `@Transactional` 어노테이션도 AOP 기반으로 동작합니다.
  * 트랜잭션 시작/커밋/롤백 로직은 내부적으로 프록시 객체가 호출 전후에 처리합니다.
  * `@Transactional`은 메서드 또는 클래스에 붙으며, 주로 Service 계층에서 선언합니다.
  * Propagation, Isolation, rollbackFor 등 고급 트랜잭션 제어 옵션도 제공됩니다.

#### LogAspect 예시

```java
@Aspect
@Component
public class LogAspect {
    // 메서드 실행 전 로깅
    @Before("execution(* com.study.springflow.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("[LogAspect] 컨트롤러 메서드 실행 전: " + 
                           joinPoint.getSignature().getDeclaringTypeName() + "." + 
                           joinPoint.getSignature().getName());
    }
    
    // 메서드 실행 전후 로깅 및 성능 측정
    @Around("execution(* com.study.springflow.controller.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        try {
            // 메서드 실행
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("[LogAspect] " + joinPoint.getSignature().getName() + 
                               " 메서드 실행 시간: " + (end - start) + "ms");
        }
    }
    
    // 메서드 정상 반환 후 로깅
    @AfterReturning(
        pointcut = "execution(* com.study.springflow.service.*.*(..))",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("[LogAspect] 메서드 정상 반환: " + 
                          joinPoint.getSignature().getName() + ", 결과: " + result);
    }
    
    // 메서드에서 예외 발생 시 로깅
    @AfterThrowing(
        pointcut = "execution(* com.study.springflow.service.*.*(..))",
        throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        System.out.println("[LogAspect] 메서드에서 예외 발생: " + 
                          joinPoint.getSignature().getName() + ", 예외: " + exception.getMessage());
    }
}
```

#### 트랜잭션 로깅 AOP 예시

```java
@Aspect
@Component
@Order(1) // 낮은 숫자가 먼저 실행됨
public class TransactionLogAspect {
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
```

### 5. `controller`

DispatcherServlet 이후 요청을 처리하는 실제 엔드포인트.

* **HelloController**: `/hello` 요청을 받아 단순 메시지 응답

  * 요청 흐름 시 Filter → Interceptor → Controller 실행 확인 가능

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        System.out.println("[HelloController] hello() 메서드 실행");
        return "Hello, SpringFlow!";
    }
    
    @GetMapping("/error-test")
    public String errorTest() {
        System.out.println("[HelloController] errorTest() 메서드 실행");
        throw new RuntimeException("테스트 예외 발생");
    }
}
```

### 6. `advice`

글로벌 예외 핸들링 처리

* **GlobalExceptionHandler**: `@ControllerAdvice` + `@ExceptionHandler`로 예외를 통합 처리

  * 예외 종류별 JSON 응답 통일

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        System.out.println("[GlobalExceptionHandler] 예외 처리: " + ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // 특정 예외 타입별 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
```

### 7. `service`

비즈니스 로직 구현 계층이며 AOP 테스트, 트랜잭션 실험을 위한 공간입니다.

* `@Service` 클래스 내부에서 DB 접근 시 `@Transactional` 테스트 가능
* 트랜잭션 롤백, readOnly, propagation 실험 가능

```java
@Service
public class ExampleService {
    private final ExampleRepository repository;
    
    @Autowired
    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }
    
    // 읽기 전용 트랜잭션 - 조회 성능 최적화
    @Transactional(readOnly = true)
    public SomeEntity findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
    }
    
    // 기본 쓰기 트랜잭션 - 저장/수정
    @Transactional
    public SomeEntity save(SomeEntity entity) {
        return repository.save(entity);
    }
    
    // 트랜잭션 전파 속성 테스트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processWithNewTransaction(Long id) {
        // 항상 새로운 트랜잭션으로 실행
        // 외부 트랜잭션이 롤백되어도 이 메서드 내용은 커밋됨
    }
    
    // 롤백 테스트
    @Transactional
    public void deleteWithPossibleRollback(Long id, boolean simulateError) {
        repository.deleteById(id);
        
        if (simulateError) {
            throw new RuntimeException("의도적인 롤백 테스트");
        }
    }
}
```

### 8. `security`

SpringFlow 프로젝트는 Spring Security와 JWT를 활용한 보안 기능을 포함하고 있습니다. 자세한 내용은 [security/README.md](security/README.md) 파일을 참조하세요.

#### 보안 기능 요약

1. **JWT 기반 인증**: 클라이언트는 사용자 인증 후 JWT 토큰을 발급받아 요청 시 사용합니다.
2. **ROLE 기반 권한 부여**: 각 엔드포인트는 ADMIN, USER 등 특정 권한이 필요합니다.
3. **리소스 소유자 확인**: 자신의 정보만 수정 가능하도록 제한됩니다(관리자 제외).
4. **비밀번호 암호화**: 사용자 비밀번호는 BCrypt로 암호화되어 저장됩니다.

#### API 엔드포인트

보안 관련 주요 API 엔드포인트:

- **회원가입**: `POST /api/auth/register`
- **로그인**: `POST /api/auth/login`
- **현재 회원 정보**: `GET /api/members/me`
- **회원 상세 조회**: `GET /api/members/{id}`
- **비밀번호 변경**: `PUT /api/members/{id}/password`
- **회원 삭제**: `DELETE /api/members/{id}`

자세한 API 사용법과 테스트 방법은 [security/README.md](security/README.md)를 참조하세요.

### 9. `swagger`

Swagger UI를 통해 API 문서화 및 테스트가 가능합니다. 자세한 내용은 [swagger/README.md](swagger/README.md) 파일을 참조하세요.

#### Swagger UI 접근 방법

애플리케이션 실행 후, 브라우저에서 아래 URL로 접근하세요:
```
http://localhost:8080/swagger-ui/index.html
```

#### 주요 기능

- **API 그룹 확인**: 인증 API, 회원 관리 API 등
- **JWT 인증 테스트**: 토큰 발급 및 인증이 필요한 API 테스트
- **요청/응답 스키마 확인**: 각 API의 입출력 데이터 구조 확인
- **API 직접 테스트**: "Try it out" 기능을 통한 API 호출 테스트

자세한 사용법은 [swagger/README.md](swagger/README.md)를 참조하세요.

### 추가 테스트 시나리오 (테스트 및 확장 가능한 기능들 섹션에 추가)

### 4. 보안 기능 테스트 시나리오

1. **권한 기반 접근 테스트**:
  - 일반 사용자로 로그인 후 관리자 전용 API 호출 시 403 에러 확인
  - 관리자로 로그인 후 동일 API 호출 시 성공 확인

2. **리소스 소유자 확인 테스트**:
  - 사용자 A로 로그인 후 사용자 B의 정보 수정 시도 시 403 에러 확인
  - 관리자로 로그인 후 모든 사용자 정보 수정 가능 확인

3. **토큰 만료 테스트**:
  - 만료된 토큰으로 요청 시 401 에러 확인
  - 로그인으로 새 토큰 발급 후 동일 요청 성공 확인

### 실행 방법에 추가

5. Swagger UI 접속
```
http://localhost:8080/swagger-ui/index.html
```

---

## 테스트 및 확장 가능한 기능들

### 1. 트랜잭션 전파 속성 실험

다양한 전파 속성(Propagation)을 테스트하여 트랜잭션 동작을 이해할 수 있습니다:

* **REQUIRED (기본값)**: 외부 트랜잭션이 있으면 참여, 없으면 새로 생성
* **REQUIRES_NEW**: 항상 새로운 트랜잭션을 생성 (기존 트랜잭션은 일시 중단)
* **NESTED**: 외부 트랜잭션 내에서 중첩 트랜잭션 생성 (부분 롤백 가능)
* **SUPPORTS**: 외부 트랜잭션이 있으면 참여, 없으면 비트랜잭션으로 실행
* **NOT_SUPPORTED**: 비트랜잭션으로 실행 (기존 트랜잭션은 일시 중단)
* **NEVER**: 비트랜잭션으로 실행 (외부 트랜잭션이 있으면 예외 발생)
* **MANDATORY**: 외부 트랜잭션이 있어야 실행 (없으면 예외 발생)

### 2. 격리 수준 테스트

트랜잭션 격리 수준(Isolation)을 설정하여 동시성 제어 방식을 실험할 수 있습니다:

* **DEFAULT**: 데이터베이스 기본 격리 수준 사용
* **READ_UNCOMMITTED**: 다른 트랜잭션의 커밋되지 않은 데이터 읽기 가능 (더티 리드)
* **READ_COMMITTED**: 다른 트랜잭션의 커밋된 데이터만 읽기 가능
* **REPEATABLE_READ**: 같은 트랜잭션 내에서 동일 데이터 여러번 읽을 때 일관성 보장
* **SERIALIZABLE**: 가장 높은 격리 수준 (동시성 낮음, 일관성 높음)

### 3. 요청 처리 흐름 문서화

`/docs/flow-request.md`와 같은 형태로 흐름을 상세히 문서화할 수 있습니다:

```markdown
# 요청 처리 흐름 문서화

## 일반 GET 요청 흐름

1. LoggingFilter의 doFilter() 메서드 호출
2. AuthInterceptor의 preHandle() 메서드 호출
3. LogAspect의 @Before 어드바이스 실행
4. 컨트롤러 메서드 실행
5. 트랜잭션 적용 시 TransactionLogAspect 어드바이스 실행
6. LogAspect의 @After 어드바이스 실행
7. AuthInterceptor의 postHandle() 메서드 호출
8. ViewResolver에 의한 뷰 처리
9. AuthInterceptor의 afterCompletion() 메서드 호출
10. LoggingFilter의 doFilter() 메서드 완료

## 예외 발생 시 흐름

1. LoggingFilter의 doFilter() 메서드 호출
2. AuthInterceptor의 preHandle() 메서드 호출
3. LogAspect의 @Before 어드바이스 실행
4. 컨트롤러 메서드 실행 중 예외 발생
5. LogAspect의 @AfterThrowing 어드바이스 실행
6. GlobalExceptionHandler의 @ExceptionHandler 메서드 호출
7. AuthInterceptor의 afterCompletion() 메서드 호출 (예외 포함)
8. LoggingFilter의 doFilter() 메서드 완료 (예외 처리)
```

---

이후 각 기능이 추가될 때마다 `/docs/flow-*.md` 형태로 흐름 설명과 로그 샘플, 발생 순서 등을 기록할 예정입니다.