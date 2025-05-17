package com.study.springflow.config;

import com.study.springflow.filter.LoggingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    /**
     * ✅ 서블릿 레벨 요청 필터 등록
     * - DispatcherServlet 이전 단계에서 동작
     * - 요청 URI, IP, 헤더 로깅 등 사전 처리에 유용
     * - 필터는 순서(order)에 따라 다단계로 설정 가능
     *
     * 🔧 추가 가능 설정:
     * - 특정 경로만 필터 적용: registration.addUrlPatterns("/api/*");
     * - 필터 비활성화: registration.setEnabled(false);
     * - 여러 필터 체인 구성 시 order 조절
     *
     * 🔍 추가 활용 옵션:
     * 1. 다중 필터 체인 구성:
     *    @Bean
     *    public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
     *        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
     *        CharacterEncodingFilter filter = new CharacterEncodingFilter();
     *        filter.setEncoding("UTF-8");
     *        filter.setForceEncoding(true);
     *        registration.setFilter(filter);
     *        registration.addUrlPatterns("/*");
     *        registration.setOrder(0); // 가장 먼저 실행
     *        registration.setName("encodingFilter");
     *        return registration;
     *    }
     *
     *    @Bean
     *    public FilterRegistrationBean<Filter> securityFilter() {
     *        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
     *        registration.setFilter(new SecurityFilter());
     *        registration.addUrlPatterns("/api/*", "/admin/*");
     *        registration.setOrder(2); // LoggingFilter 다음 실행
     *        registration.setName("securityFilter");
     *        return registration;
     *    }
     *
     * 2. 필터 초기화 파라미터 설정:
     *    @Bean
     *    public FilterRegistrationBean<Filter> configureableFilter() {
     *        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
     *        CustomFilter filter = new CustomFilter();
     *        registration.setFilter(filter);
     *
     *        // 초기화 파라미터 설정
     *        Map<String, String> initParams = new HashMap<>();
     *        initParams.put("logLevel", "DEBUG");
     *        initParams.put("includePayload", "true");
     *        initParams.put("maxPayloadLength", "10000");
     *        registration.setInitParameters(initParams);
     *
     *        registration.addUrlPatterns("/*");
     *        registration.setOrder(3);
     *        return registration;
     *    }
     *
     * 3. 필터 예외 처리:
     *    public class ErrorHandlingFilter implements Filter {
     *        @Override
     *        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *                throws IOException, ServletException {
     *            try {
     *                chain.doFilter(request, response);
     *            } catch (Exception e) {
     *                HttpServletResponse httpResponse = (HttpServletResponse) response;
     *                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     *                httpResponse.setContentType("application/json");
     *                httpResponse.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
     *            }
     *        }
     *    }
     *
     * 4. 요청/응답 래핑:
     *    public class ContentCachingFilter implements Filter {
     *        @Override
     *        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *                throws IOException, ServletException {
     *            ContentCachingRequestWrapper requestWrapper =
     *                new ContentCachingRequestWrapper((HttpServletRequest) request);
     *            ContentCachingResponseWrapper responseWrapper =
     *                new ContentCachingResponseWrapper((HttpServletResponse) response);
     *
     *            try {
     *                chain.doFilter(requestWrapper, responseWrapper);
     *            } finally {
     *                // 요청 본문 로깅
     *                byte[] content = requestWrapper.getContentAsByteArray();
     *                if (content.length > 0) {
     *                    System.out.println("Request body: " + new String(content));
     *                }
     *
     *                // 응답 본문 복사 (중요: 안하면 클라이언트가 빈 응답을 받음)
     *                responseWrapper.copyBodyToResponse();
     *            }
     *        }
     *    }
     *
     * 5. 보안 필터 예시:
     *    public class XssFilter implements Filter {
     *        @Override
     *        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *                throws IOException, ServletException {
     *            XssRequestWrapper wrappedRequest = new XssRequestWrapper((HttpServletRequest) request);
     *            chain.doFilter(wrappedRequest, response);
     *        }
     *
     *        // XSS 방지 래퍼 클래스
     *        private class XssRequestWrapper extends HttpServletRequestWrapper {
     *            public XssRequestWrapper(HttpServletRequest request) {
     *                super(request);
     *            }
     *
     *            @Override
     *            public String[] getParameterValues(String parameter) {
     *                String[] values = super.getParameterValues(parameter);
     *                if (values == null) return null;
     *
     *                int count = values.length;
     *                String[] encodedValues = new String[count];
     *                for (int i = 0; i < count; i++) {
     *                    encodedValues[i] = cleanXSS(values[i]);
     *                }
     *                return encodedValues;
     *            }
     *
     *            private String cleanXSS(String value) {
     *                // XSS 처리 로직 구현
     *                return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
     *            }
     *        }
     *    }
     *
     * 🔧 흐름 테스트 방법:
     * 1. MDC를 활용한 요청 추적:
     *    public class RequestTrackingFilter implements Filter {
     *        @Override
     *        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *                throws IOException, ServletException {
     *            String requestId = UUID.randomUUID().toString();
     *            MDC.put("requestId", requestId);
     *
     *            try {
     *                HttpServletRequest httpRequest = (HttpServletRequest) request;
     *                System.out.println("[" + requestId + "] 요청: " + httpRequest.getRequestURI());
     *
     *                chain.doFilter(request, response);
     *
     *                System.out.println("[" + requestId + "] 응답 완료");
     *            } finally {
     *                MDC.remove("requestId");
     *            }
     *        }
     *    }
     *
     * 2. 성능 측정 필터:
     *    public class PerformanceFilter implements Filter {
     *        @Override
     *        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *                throws IOException, ServletException {
     *            long startTime = System.currentTimeMillis();
     *
     *            try {
     *                chain.doFilter(request, response);
     *            } finally {
     *                long endTime = System.currentTimeMillis();
     *                HttpServletRequest httpRequest = (HttpServletRequest) request;
     *                System.out.println("URI: " + httpRequest.getRequestURI() +
     *                                   ", 처리 시간: " + (endTime - startTime) + "ms");
     *            }
     *        }
     *    }
     *
     * 3. 필터 체인 순서 테스트:
     *    - 여러 필터 순서대로 등록
     *    - 각 필터에서 System.out.println("필터명 - 시작"); 및 System.out.println("필터명 - 종료");
     *    - 로그 출력 순서 확인:
     *      필터1 - 시작
     *      필터2 - 시작
     *      필터3 - 시작
     *      필터3 - 종료
     *      필터2 - 종료
     *      필터1 - 종료
     */
    @Bean
    public FilterRegistrationBean<Filter> loggingFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingFilter());
        registration.addUrlPatterns("/*");                // 전체 경로에 적용
        registration.setOrder(1);                         // 실행 순서
        registration.setName("LoggingFilter");
        registration.setEnabled(true);                    // 필터 활성화 여부
        return registration;
    }
}