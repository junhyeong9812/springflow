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

## 주요 구성 예정

* `controller`: Hello API 테스트용 컨트롤러
* `filter`: 서블릿 레벨 요청 가로채기 실험
* `interceptor`: Spring MVC 레벨 인터셉터 흐름 실험
* `aop`: Aspect 기반 로깅 / 트랜잭션 감시 실험
* `config`: DispatcherServlet, ViewResolver, Filter, Interceptor 등 수동 설정
* `advice`: 예외 처리 흐름 확인용 글로벌 핸들러
* `service`: AOP 및 트랜잭션 흐름 관찰용 서비스 레이어


---

이후 각 기능이 추가될 때마다 `/docs/flow-*.md` 형태로 흐름 설명과 로그 샘플, 발생 순서 등을 기록할 예정입니다.
