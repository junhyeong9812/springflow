# SpringFlow 프로젝트

## 프로젝트 개요

**SpringFlow**는 Spring Boot를 기반으로 하지만, 자동 설정에 의존하지 않고 **Spring Framework의 수동 구성 방식**을 직접 재현하면서 **Spring 내부 동작 원리와 요청 처리 흐름(MVC 구조)을 학습**하기 위한 실습용 프로젝트입니다.

Spring의 요청 처리 흐름, 트랜잭션 처리, 예외 처리, DispatcherServlet 등록, AOP 적용 등 다양한 컴포넌트를 **직접 설정하고 흐름을 눈으로 확인하는 실험형 프로젝트**입니다.

이 프로젝트의 최종 목적은 Spring Boot의 편의성에 가려 잘 보이지 않던 **Spring의 핵심 구조와 시동 흐름**을 명확하게 이해하고, 실무 설계 시 더 **유연하고 판단력 있는 개발자**로 성장하는 데 있습니다.

## 목표

* Spring MVC 요청 처리 흐름에 대한 깊은 이해 (Filter → DispatcherServlet → HandlerMapping → Interceptor → HandlerAdapter → Controller → ViewResolver)
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
    * CGLIB는 final 클래스나 메서드에는 적용할 수 없습니다. Spring 4.0+ 버전부터는 Objenesis 라이브러리를 통해 기본 생성자 없이도 프록시 생성이 가능하지만, 일반적으로 기본 생성자를 제공하는 것이 권장됩니다.

  * **exposeProxy=true 옵션**:
    * AOP 프록시를 현재 스레드의 ThreadLocal에 노출시키는 설정입니다.
    * `AopContext.currentProxy()`를 통해 현재 실행 중인 메서드의 AOP 프록시에 접근할 수 있게 합니다.
    * Self-Invocation(자기 호출) 문제를 해결할 때 유용합니다.

  * **Self-Invocation 문제**:
    * 같은 클래스 내에서 메서드 A가 메서드 B를 호출할 때, B에 적용된 어드바이스(예: @Transactional)가 작동하지 않는 문제입니다.
    * 내부 호출은 프록시를 통하지 않고 직접 대상 객체의 메서드를 호출하기 때문에 발생합니다.
    * Self-Invocation 문제를 해결하는 방법:
      1. `@Lazy`와 `@Autowired`를 사용해 자기 자신을 주입받아 프록시를 통해 호출
      2. `AopContext.currentProxy()`를 사용 (exposeProxy=true 설정 필요)
      3. 별도의 서비스 클래스로 분리하여 외부 호출로 변경

  * **수동 Advisor 등록**:
    * Advisor는 Pointcut(어디에 적용할지)과 Advice(무엇을 적용할지)를 결합한 객체입니다.
    * 어노테이션 대신 프로그래밍 방식으로 AOP를 설정할 때 사용합니다.
    * 세밀한 제어가 필요하거나, 런타임에 AOP 설정을 변경해야 할 때 유용합니다.
    * 예: `DefaultPointcutAdvisor`를 빈으로 등록하여 특정 패턴의 메서드에 로깅 또는 트랜잭션 기능을 적용

#### DispatcherConfig

* `DispatcherServlet`을 명시적으로 수동 등록하여 요청 진입점 흐름을 실험합니다.

  * **DispatcherServlet이란?**:
    * Spring MVC의 핵심 컴포넌트로, Front Controller 패턴을 구현한 클래스입니다.
    * 클라이언트의 요청은 먼저 Filter를 거친 뒤 DispatcherServlet이 받아서 처리 흐름을 제어합니다.
    * 내부적으로 HandlerMapping, HandlerAdapter, ViewResolver 등과 협력해 요청을 컨트롤러에 위임하고, 응답을 뷰로 전달합니다.
    * 보통 Spring Boot에서는 자동으로 등록되지만, 본 프로젝트에서는 명시적으로 등록하여 요청 흐름을 관찰합니다.

#### FilterConfig

* `FilterRegistrationBean`을 통해 서블릿 레벨 필터를 등록합니다.

#### ViewResolverConfig

* `InternalResourceViewResolver`를 직접 등록하여 JSP 뷰를 처리합니다.
* 주의: Spring Boot 3.x는 기본적으로 JSP를 지원하지 않으므로, JSP 사용 시 별도의 의존성 추가와 설정이 필요합니다. (spring-boot-starter-web에는 Tomcat Embed Jasper가 포함되지 않음)

#### WebMvcConfig

* **WebMvcConfigurer 인터페이스**를 구현하여 다음 설정을 수동으로 구성합니다:

  * **Interceptor**: Spring MVC에서 컨트롤러 실행 전후 로직을 삽입할 수 있는 컴포넌트
  * **CORS**: Cross-Origin Resource Sharing 설정
  * **ViewController**: 단순 뷰 이동을 위한 URL → View 매핑 설정
  * **MessageConverter**: 요청/응답 변환기(Jackson, XML 등)를 확장하거나 교체 가능
  * **HandlerExceptionResolver**: 예외를 뷰 또는 JSON 응답으로 매핑

### 2. `filter`

Servlet container 레벨에서 동작하며, 요청이 DispatcherServlet에 도달하기 전에 사전 처리 역할을 합니다.

#### LoggingFilter

* `javax.servlet.Filter`를 구현하여 요청 URL, 메서드, 클라이언트 IP 등을 로깅합니다.

  * `init()` / `doFilter()` / `destroy()`를 통해 생명주기 확인
  * 순서 설정으로 다단계 필터링 구현 가능
  * `FilterRegistrationBean`으로 설정

#### Filter vs Interceptor 비교

**Filter (서블릿 필터)**:
- Servlet Container 레벨에서 동작
- DispatcherServlet 호출 이전에 실행
- 요청/응답 객체 변경 가능
- Spring 컨텍스트 접근이 제한적(직접 DI 어려움)(@Component로 등록한 필터이거나 WebApplicationContextUtils로 Bean을 얻으면 접근 가능하므로 “제한적”)
- 주로 인코딩, 보안, 로깅 등 전역적 처리에 사용

**Interceptor (스프링 인터셉터)**:
- Spring MVC 레벨에서 동작
- DispatcherServlet 이후, Controller 호출 전후에 실행
- Spring Bean에 접근 가능
- HandlerMethod 정보 접근 가능
- 주로 인증, 로깅, Controller 관련 공통 처리에 사용

### 3. `interceptor`

Spring MVC의 HandlerMapping → Controller 진입 전/후를 제어할 수 있는 레벨입니다.

* **정의**: `HandlerInterceptor`를 구현해 요청 흐름을 중간에 가로채고, 사전/사후 로직을 실행
* **AuthInterceptor**: 인증이나 공통 로직 처리에 활용됩니다.

  * `preHandle()`에서 로그인 확인, 권한 체크 가능
  * `postHandle()`에서 로깅, 리다이렉션 처리
  * `afterCompletion()`에서 리소스 정리
* **HandlerMapping**: 요청 URL·HTTP 메서드 등의 정보를 바탕으로 어떤 컨트롤러(Handler)가 처리할지 찾아주는 컴포넌트입니다. DispatcherServlet이 가장 먼저 참조하며, 결과를 HandlerAdapter로 전달합니다.


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

## 테스트 및 확장 가능한 기능들

### 1. 트랜잭션 전파 속성 실험

다양한 전파 속성(Propagation)을 테스트하여 트랜잭션 동작을 이해할 수 있습니다:

* **REQUIRED (기본값)**: 외부 트랜잭션이 있으면 참여, 없으면 새로 생성
* **REQUIRES_NEW**: 항상 새로운 트랜잭션을 생성 (기존 트랜잭션은 일시 중단)
* **NESTED**: 외부 트랜잭션 내에서 중첩 트랜잭션 생성 (부분 롤백 가능)
  - 주의: 데이터베이스가 Savepoint를 지원해야 함 (MySQL, PostgreSQL 등)
  - H2, Oracle 등에서 지원하지만 모든 데이터베이스에서 사용 가능하지는 않음
* **SUPPORTS**: 외부 트랜잭션이 있으면 참여, 없으면 비트랜잭션으로 실행
* **NOT_SUPPORTED**: 비트랜잭션으로 실행 (기존 트랜잭션은 일시 중단)
* **NEVER**: 비트랜잭션으로 실행 (외부 트랜잭션이 있으면 예외 발생)
* **MANDATORY**: 외부 트랜잭션이 있어야 실행 (없으면 예외 발생)

### 2. 격리 수준 테스트

트랜잭션 격리 수준(Isolation)을 설정하여 동시성 제어 방식을 실험할 수 있습니다:

* **DEFAULT**: 데이터베이스 기본 격리 수준 사용
* **READ_UNCOMMITTED**: 다른 트랜잭션의 커밋되지 않은 데이터 읽기 가능 (더티 리드, 반복 불가능한 읽기, 팬텀 읽기 모두 발생 가능)
* **READ_COMMITTED**: 다른 트랜잭션의 커밋된 데이터만 읽기 가능 (더티 리드 방지, 하지만 반복 불가능한 읽기와 팬텀 읽기는 발생 가능)
* **REPEATABLE_READ**: 같은 트랜잭션 내에서 동일 데이터 여러번 읽을 때 일관성 보장 (더티 리드, 반복 불가능한 읽기 방지. 팬텀 읽기는 데이터베이스에 따라 다름 - MySQL InnoDB는 방지, 일반 SQL 표준은 발생 가능)
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

## 실행 방법

1. 프로젝트 클론
```bash
git clone <repository-url>
cd springflow
```

2. 의존성 설치 및 빌드
```bash
./gradlew clean build
```

3. 애플리케이션 실행
```bash
./gradlew bootRun
```

4. 기본 엔드포인트 확인
```
http://localhost:8080/hello
```

5. Swagger UI 접속
```
http://localhost:8080/swagger-ui/index.html
```

---

이후 각 기능이 추가될 때마다 `/docs/flow-*.md` 형태로 흐름 설명과 로그 샘플, 발생 순서 등을 기록할 예정입니다.