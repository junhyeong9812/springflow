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

* `@EnableAspectJAutoProxy` 활성화를 통한 AOP 기능 설정
* **테스트 가능한 기능:**
  * `proxyTargetClass=true` 옵션을 통한 CGLIB 기반 프록시 생성 테스트
  * `exposeProxy=true` 설정으로 AopContext를 통한 self-invocation 문제 해결 테스트
  * 수동 Advisor 등록을 통한 세밀한 AOP 제어
  * 트랜잭션 AOP 설정 및 동작 확인

#### DispatcherConfig

* `DispatcherServlet` 수동 등록 및 설정
* **테스트 가능한 기능:**
  * 다중 DispatcherServlet 등록 (URL 패턴별 다른 서블릿 매핑)
  * 개별 DispatcherServlet에 별도의 ApplicationContext 연결
  * 서블릿 초기화 파라미터 설정
  * DispatcherServlet 확장을 통한 요청 처리 로직 수정

#### FilterConfig

* 서블릿 필터 등록 및 설정
* **테스트 가능한 기능:**
  * 다중 필터 체인 구성 (인증, 로깅, 인코딩 등)
  * URL 패턴별 필터 적용
  * 필터 실행 순서 제어
  * 필터 초기화 파라미터 설정
  * 요청/응답 래핑을 통한 본문 수정 테스트

#### ViewResolverConfig

* `InternalResourceViewResolver` 수동 등록
* **테스트 가능한 기능:**
  * 다중 ViewResolver 체인 설정
  * ContentNegotiatingViewResolver를 통한 응답 형식 협상
  * Thymeleaf, FreeMarker 등 다양한 템플릿 엔진 연동
  * JSON/XML 응답용 View 등록
  * 예외 처리용 View 매핑

#### WebMvcConfig

* Spring MVC의 다양한 기능 설정
* **테스트 가능한 기능:**
  * 정적 리소스 핸들러 설정 및 캐싱
  * CORS 설정 (도메인, 메서드, 헤더 등)
  * 인터셉터 등록 및 URL 패턴 매핑
  * 뷰 컨트롤러를 통한 간단한 뷰 매핑
  * 메시지 컨버터 설정
  * 경로 매칭 옵션 설정
  * 컨트롤러 메서드 인자 리졸버 등록
  * 비동기 요청 처리 설정

### 2. `filter`

서블릿 컨테이너 레벨에서 요청을 사전/사후 처리하는 필터 컴포넌트입니다.

#### LoggingFilter

* HTTP 요청 정보 로깅 필터
* **테스트 가능한 기능:**
  * 필터 생명주기 메서드 (`init`, `doFilter`, `destroy`) 실행 확인
  * 요청/응답 본문 로깅 구현
  * MDC를 활용한 요청 추적
  * 헤더 정보 로깅
  * 성능 측정 및 임계치 초과 시 경고 로그

#### 확장 가능한 필터 기능

* 인증 필터 구현 (JWT, 세션 등)
* 요청/응답 압축 필터
* 문자 인코딩 필터
* CORS 필터
* XSS 방지 필터
* 요청 추적 ID 필터

### 3. `interceptor`

Spring MVC 내부에서 컨트롤러 실행 전/후에 동작하는 인터셉터 컴포넌트입니다.

#### AuthInterceptor

* 인증 관련 인터셉터
* **테스트 가능한 기능:**
  * `preHandle` - 컨트롤러 실행 전 (인증 검사, 권한 확인)
  * `postHandle` - 컨트롤러 실행 후, 뷰 렌더링 전 (모델 데이터 추가)
  * `afterCompletion` - 뷰 렌더링 후 (리소스 정리, 로깅)
  * 어노테이션 기반 권한 검사 (`@AdminOnly`, `@RequiredPermission` 등)
  * 실행 순서 제어 및 여러 인터셉터 체인 구성

#### 확장 가능한 인터셉터 기능

* 로깅 인터셉터
* 성능 측정 인터셉터
* 지역화(i18n) 인터셉터
* 감사(Auditing) 인터셉터
* 세션 관리 인터셉터
* API 버전 관리 인터셉터

### 4. `aop`

핵심 비즈니스 로직과 분리된 공통 관심사를 처리하는 AOP 컴포넌트입니다.

#### LogAspect

* 로깅 관련 AOP 기능
* **테스트 가능한 기능:**
  * `@Before` - 메서드 실행 전 로깅
  * `@After` - 메서드 실행 후 로깅
  * `@Around` - 메서드 실행 전후 로깅 및 성능 측정
  * `@AfterReturning` - 메서드 정상 반환 시 로깅
  * `@AfterThrowing` - 메서드 예외 발생 시 로깅
  * 포인트컷 표현식을 통한 적용 대상 지정

#### TransactionLogAspect

* 트랜잭션 관련 로깅 AOP
* **테스트 가능한 기능:**
  * `@Transactional` 어노테이션이 적용된 메서드 추적
  * 트랜잭션 시작/커밋/롤백 흐름 로깅
  * 트랜잭션 속성(전파 속성, 격리 수준 등) 확인
  * 트랜잭션 실행 시간 측정

#### 확장 가능한 AOP 기능

* 보안 관련 검사 (메서드 수준 인가)
* 캐싱 AOP
* 재시도 로직 AOP
* 데이터 유효성 검사 AOP
* API 속도 제한 AOP
* 메서드 호출 추적 AOP

### 5. `controller`

HTTP 요청을 처리하고 비즈니스 로직을 호출하는 컨트롤러 컴포넌트입니다.

#### HelloController

* 간단한 테스트용 REST 컨트롤러
* **테스트 가능한 기능:**
  * `@RestController` 및 `@Controller` 차이점 테스트
  * 다양한 HTTP 메서드 매핑 (`@GetMapping`, `@PostMapping` 등)
  * 경로 변수 및 요청 파라미터 처리
  * 요청 본문 바인딩
  * 응답 상태 코드 제어

#### MemberController

* 회원 관리 REST API 컨트롤러
* **테스트 가능한 기능:**
  * 회원 CRUD 작업 처리
  * DTO ↔ 엔티티 변환
  * 유효성 검사 (`@Valid`)
  * 예외 처리 및 오류 응답 생성
  * ResponseEntity를 통한 응답 구성

### 6. `advice`

컨트롤러에서 발생하는 예외를 중앙에서 처리하는 컴포넌트입니다.

#### GlobalExceptionHandler

* 전역 예외 처리기
* **테스트 가능한 기능:**
  * `@ControllerAdvice` 및 `@RestControllerAdvice` 설정
  * `@ExceptionHandler`를 통한 예외 타입별 처리
  * 일관된 오류 응답 구조 생성
  * 예외 상황별 HTTP 상태 코드 매핑
  * 로깅 및 알림 기능 추가

#### 확장 가능한 어드바이스 기능

* `@ModelAttribute` 어드바이스로 모든 모델에 공통 데이터 추가
* 요청 본문, 응답 본문 변환 어드바이스
* API 버전 정보 어드바이스
* HATEOAS 링크 추가 어드바이스

### 7. `service`

비즈니스 로직을 구현하고 트랜잭션을 관리하는 서비스 컴포넌트입니다.

#### MemberService

* 회원 관리 비즈니스 로직
* **테스트 가능한 기능:**
  * `@Transactional` 기본 사용법
  * 읽기 전용 트랜잭션 (`readOnly = true`)
  * 트랜잭션 전파 속성 테스트 (`propagation = Propagation.REQUIRED` 등)
  * 트랜잭션 격리 수준 테스트 (`isolation = Isolation.READ_COMMITTED` 등)
  * 특정 예외에 대한 롤백 설정 (`rollbackFor = Exception.class`)
  * 트랜잭션 타임아웃 설정 (`timeout = 10`)

#### 확장 가능한 서비스 기능

* 이벤트 발행 (ApplicationEventPublisher)
* 캐싱 (`@Cacheable`, `@CacheEvict` 등)
* 비동기 처리 (`@Async`)
* 스케줄링 (`@Scheduled`)
* 재시도 로직 (`@Retryable`)
* 분산 락 적용

### 8. `entity`

JPA를 사용한 데이터베이스 엔티티 클래스입니다.

#### Member

* 회원 정보 엔티티
* **테스트 가능한 기능:**
  * 기본적인 ORM 매핑 (`@Entity`, `@Table`, `@Column` 등)
  * 식별자 생성 전략 (`@GeneratedValue`)
  * 관계 매핑 (`@OneToMany`, `@ManyToOne` 등)
  * 열거형 매핑 (`@Enumerated`)
  * 생명주기 콜백 (`@PrePersist`, `@PostLoad` 등)
  * 임베디드 값 타입 (`@Embedded`)

#### 확장 가능한 엔티티 기능

* 상속 관계 매핑 (`@Inheritance`)
* 낙관적 락 (`@Version`)
* 동적 쿼리를 위한 메타모델 생성
* 감사(Auditing) 정보 관리
* 소프트 삭제 구현
* 커스텀 ID 생성기

### 9. `repository`

데이터 접근 계층을 담당하는 레포지토리 컴포넌트입니다.

#### MemberRepository

* 회원 엔티티 데이터 액세스 인터페이스
* **테스트 가능한 기능:**
  * Spring Data JPA 기본 CRUD 메서드
  * 메서드 이름 기반 쿼리 생성
  * `@Query`를 사용한 JPQL 쿼리
  * 네이티브 SQL 쿼리 사용 (`nativeQuery = true`)
  * 페이징 및 정렬 처리
  * 동적 Specification 또는 QueryDSL 사용

#### 확장 가능한 레포지토리 기능

* 커스텀 레포지토리 구현
* 프로젝션을 통한 부분 데이터 조회
* 비동기 쿼리 메서드 (`@Async`)
* 저장소 감사(Auditing) 기능
* 대량 배치 작업
* N+1 문제 해결을 위한 fetch join

### 10. `docs`

프로젝트의 핵심 흐름과 테스트 결과를 문서화합니다.

#### flow-request.md

* 요청 처리 흐름 문서
* **문서화 가능한 항목:**
  * Filter → Interceptor → Controller → Service → Repository 흐름 로그
  * 트랜잭션 시작/커밋/롤백 로그
  * 예외 처리 흐름 로그
  * AOP 적용 전후 로그
  * 컴포넌트 실행 순서 다이어그램

#### flow-transaction.md

* 트랜잭션 처리 흐름 문서
* **문서화 가능한 항목:**
  * 트랜잭션 전파 속성별 동작 차이
  * 트랜잭션 격리 수준별 동작 차이
  * 트랜잭션 롤백 시나리오
  * 중첩 트랜잭션 테스트 결과

#### flow-aop.md

* AOP 처리 흐름 문서
* **문서화 가능한 항목:**
  * AOP 프록시 생성 과정
  * 어드바이스 실행 순서
  * 포인트컷 표현식별 적용 범위
  * AOP와 트랜잭션 연동 방식

## 실행 및 테스트 방법

### 1. 기본 설정 테스트

* 애플리케이션 시작 시 Bean 등록 로그 확인
* `http://localhost:8080/hello` 접속으로 기본 요청 처리 흐름 확인
* H2 콘솔 접속 (`http://localhost:8080/h2-console`)으로 데이터베이스 확인

### 2. 요청 처리 흐름 테스트

* Filter → Interceptor → Controller → Service 흐름 추적
* 로그를 통한 각 단계별 실행 시점 확인
* HTTP 메서드별 처리 방식 비교 (GET, POST, PUT, DELETE)

### 3. 트랜잭션 테스트

* `@Transactional` 적용 메서드 동작 확인
* 프록시 기반 트랜잭션 처리 분석
* 롤백 테스트 (`simulateError=true` 파라미터 사용)
* 읽기 전용 트랜잭션과 일반 트랜잭션 성능 비교

### 4. AOP 테스트

* 로깅 AOP를 통한 메서드 실행 시간 측정
* 다양한 어드바이스 타입 적용 및 실행 순서 확인
* 포인트컷 표현식 변경을 통한 AOP 적용 범위 테스트
* AOP와 트랜잭션 연동 테스트

### 5. 예외 처리 테스트

* 의도적 예외 발생 테스트 (`/error-test` 엔드포인트)
* `@ExceptionHandler`를 통한 예외 처리 흐름 확인
* 트랜잭션 롤백 연동 테스트
* 예외 발생 시 로깅 및 응답 포맷 검증

---

