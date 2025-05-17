package com.study.springflow.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class DispatcherConfig {

    /**
     * ✅ DispatcherServlet 수동 등록
     * - Spring Boot는 기본적으로 "/" 경로에 자동 등록
     * - 여기서는 명시적으로 수동 등록하여 흐름 제어 가능
     *
     * 🔍 추가 설정:
     * - registration.addUrlMappings("/api/*") → 다중 서블릿 경로 분기 가능
     * - registration.setInitParameters(...) → 서블릿 초기 설정 가능
     * - DispatcherServlet의 Multipart 설정 분리 관리도 가능
     *
     * 🔍 추가 활용 옵션:
     * 1. 다중 DispatcherServlet 등록:
     *    @Bean
     *    public DispatcherServlet apiDispatcherServlet() {
     *        DispatcherServlet dispatcherServlet = new DispatcherServlet();
     *        // API 전용 DispatcherServlet 설정
     *        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
     *        return dispatcherServlet;
     *    }
     *
     *    @Bean
     *    public ServletRegistrationBean<DispatcherServlet> apiServletRegistration() {
     *        ServletRegistrationBean<DispatcherServlet> registration =
     *            new ServletRegistrationBean<>(apiDispatcherServlet(), "/api/*");
     *        registration.setName("apiDispatcherServlet");
     *        registration.setLoadOnStartup(2);
     *        return registration;
     *    }
     *
     * 2. 서블릿 컨텍스트 분리:
     *    @Bean
     *    public DispatcherServlet adminDispatcherServlet() {
     *        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
     *        context.register(AdminWebConfig.class); // 관리자용 별도 설정
     *
     *        DispatcherServlet servlet = new DispatcherServlet(context);
     *        servlet.setPublishEvents(true);  // 이벤트 발행 활성화
     *        servlet.setEnableLoggingRequestDetails(true); // 요청 세부사항 로깅
     *        return servlet;
     *    }
     *
     *    @Bean
     *    public ServletRegistrationBean<DispatcherServlet> adminServletRegistration() {
     *        ServletRegistrationBean<DispatcherServlet> registration =
     *            new ServletRegistrationBean<>(adminDispatcherServlet(), "/admin/*");
     *        registration.setName("adminDispatcherServlet");
     *        return registration;
     *    }
     *
     * 3. 서블릿 초기화 파라미터 설정:
     *    @Bean
     *    public ServletRegistrationBean<DispatcherServlet> customizedDispatcherServlet() {
     *        ServletRegistrationBean<DispatcherServlet> registration =
     *            new ServletRegistrationBean<>(new DispatcherServlet());
     *
     *        // 초기 파라미터 설정
     *        Map<String, String> params = new HashMap<>();
     *        params.put("throwExceptionIfNoHandlerFound", "true");
     *        params.put("contextConfigLocation", "classpath:custom-servlet-context.xml");
     *        params.put("detectAllHandlerMappings", "false");
     *        registration.setInitParameters(params);
     *
     *        registration.addUrlMappings("/custom/*");
     *        return registration;
     *    }
     *
     * 4. DispatcherServlet 확장:
     *    public class CustomDispatcherServlet extends DispatcherServlet {
     *        @Override
     *        protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
     *            System.out.println("요청 처리 시작: " + request.getRequestURI());
     *            try {
     *                super.doService(request, response);
     *            } finally {
     *                System.out.println("요청 처리 종료: " + request.getRequestURI());
     *            }
     *        }
     *    }
     *
     * 5. 멀티파트 설정:
     *    @Bean
     *    public DispatcherServlet fileUploadDispatcherServlet() {
     *        DispatcherServlet servlet = new DispatcherServlet();
     *        servlet.setMultipartResolver(new StandardServletMultipartResolver());
     *        return servlet;
     *    }
     *
     *    @Bean
     *    public ServletRegistrationBean<DispatcherServlet> fileUploadServletRegistration() {
     *        ServletRegistrationBean<DispatcherServlet> registration =
     *            new ServletRegistrationBean<>(fileUploadDispatcherServlet(), "/upload/*");
     *
     *        // 멀티파트 설정
     *        registration.setMultipartConfig(
     *            new MultipartConfigElement("/tmp/uploads", 5 * 1024 * 1024, 25 * 1024 * 1024, 1 * 1024 * 1024)
     *        );
     *
     *        return registration;
     *    }
     *
     * 🔧 흐름 테스트 방법:
     * 1. DispatcherServlet 초기화 과정 추적:
     *    - 확장 클래스 생성하여 로깅
     *    public class LoggingDispatcherServlet extends DispatcherServlet {
     *        @Override
     *        protected void initStrategies(ApplicationContext context) {
     *            System.out.println("=== DispatcherServlet 초기화 시작 ===");
     *            super.initStrategies(context);
     *            System.out.println("=== DispatcherServlet 초기화 완료 ===");
     *        }
     *    }
     *
     * 2. DispatcherServlet 처리 흐름 분석:
     *    - 각 단계마다 로그 추가
     *    @Override
     *    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
     *        System.out.println("1. 요청 진입: " + request.getRequestURI());
     *        try {
     *            // 핸들러 조회
     *            HandlerExecutionChain mappedHandler = getHandler(request);
     *            System.out.println("2. 핸들러 매핑: " + (mappedHandler != null ? mappedHandler.getHandler() : "없음"));
     *
     *            // 이하 생략...
     *            super.doDispatch(request, response);
     *        } finally {
     *            System.out.println("9. 요청 처리 완료");
     *        }
     *    }
     *
     * 3. 여러 DispatcherServlet 간 URL 매핑 테스트:
     *    - /api/v1/users → apiDispatcherServlet
     *    - /admin/dashboard → adminDispatcherServlet
     *    - / → 기본 dispatcherServlet
     */
    @Bean
    public ServletRegistrationBean<DispatcherServlet> dispatcherServlet(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/");
        registration.setName("dispatcherServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }
}