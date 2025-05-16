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

* **WebMvcConfig**: 다음 설정을 수동 구성합니다:

    * **Interceptor**: Spring MVC에서 컨트롤러 실행 전후 로직을 삽입할 수 있는 컴포넌트. 인증/인가, 로깅, 요청 검증 등에 활용됨
    * **CORS**: Cross-Origin Resource Sharing 설정. 클라이언트 도메인과 서버 도메인이 다를 때, 어떤 Origin의 요청을 허용할지 지정함
    * **ViewController**: 단순 뷰 이동을 위한 URL → View 매핑 설정
    * **MessageConverter**: 요청/응답 변환기(Jackson, XML 등)를 확장하거나 교체 가능
    * **HandlerExceptionResolver**: 예외를 뷰 또는 JSON 응답으로 매핑

* **FilterConfig**: `FilterRegistrationBean`을 통해 서블릿 레벨 필터를 등록합니다.

* **AopConfig**: `@EnableAspectJAutoProxy`를 사용하여 AOP 설정을 수동으로 명시합니다.

* **ViewResolverConfig**: `InternalResourceViewResolver`를 직접 등록하여 JSP 뷰를 처리합니다.

* **DispatcherConfig**: `DispatcherServlet`을 명시적으로 수동 등록하여 요청 진입점 흐름을 실험합니다.

    * **DispatcherServlet이란?**

        * Spring MVC의 핵심 컴포넌트로, Front Controller 패턴을 구현한 클래스입니다.
        * 클라이언트의 모든 요청은 DispatcherServlet이 가장 먼저 받아 처리 흐름을 제어합니다.
        * 내부적으로 HandlerMapping, HandlerAdapter, ViewResolver 등과 협력해 요청을 컨트롤러에 위임하고, 응답을 뷰로 전달합니다.
        * 보통 Spring Boot에서는 자동으로 등록되지만, 본 프로젝트에서는 명시적으로 등록하여 요청 흐름을 관찰합니다.

### 2. `filter`

Servlet container 레벨에서 동작하며, 요청이 DispatcherServlet에 도달하기 전에 사전 처리 역할을 합니다.

* **LoggingFilter**: `javax.servlet.Filter`를 구현하여 요청 URL, 메서드, 클라이언트 IP 등을 로깅합니다.

    * `init()` / `doFilter()` / `destroy()`를 통해 생명주기 확인
    * 순서 설정으로 다단계 필터링 구현 가능
    * `FilterRegistrationBean`으로 설정

### 3. `interceptor`

Spring MVC의 HandlerMapping → Controller 진입 전/후를 제어할 수 있는 레벨입니다.

* **정의**: `HandlerInterceptor`를 구현해 요청 흐름을 중간에 가로채고, 사전/사후 로직을 실행
* **AuthInterceptor**: 인증이나 공통 로직 처리에 활용됩니다.

    * `preHandle()`에서 로그인 확인, 권한 체크 가능
    * `postHandle()`에서 로깅, 리다이렉션 처리
    * `afterCompletion()`에서 리소스 정리

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

### 5. `controller`

DispatcherServlet 이후 요청을 처리하는 실제 엔드포인트.

* **HelloController**: `/hello` 요청을 받아 단순 메시지 응답

    * 요청 흐름 시 Filter → Interceptor → Controller 실행 확인 가능

### 6. `advice`

글로벌 예외 핸들링 처리

* **GlobalExceptionHandler**: `@ControllerAdvice` + `@ExceptionHandler`로 예외를 통합 처리

    * 예외 종류별 JSON 응답 통일

### 7. `service`

비즈니스 로직 구현 계층이며 AOP 테스트, 트랜잭션 실험을 위한 공간입니다.

* `@Service` 클래스 내부에서 DB 접근 시 `@Transactional` 테스트 가능
* 트랜잭션 롤백, readOnly, propagation 실험 가능

---

이후 각 기능이 추가될 때마다 `/docs/flow-*.md` 형태로 흐름 설명과 로그 샘플, 발생 순서 등을 기록할 예정입니다.
