package com.study.springflow.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LoggingFilter implements Filter {

    /**
     * ✅ 로깅 필터
     * - 모든 HTTP 요청에 대한 기본 정보를 로깅
     * - Filter 인터페이스의 생명주기 메서드(init, doFilter, destroy) 구현
     *
     * 🔍 추가 활용 옵션:
     * 1. 요청/응답 본문 로깅:
     *    @Override
     *    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *            throws IOException, ServletException {
     *        HttpServletRequest httpRequest = (HttpServletRequest) request;
     *        String uri = httpRequest.getRequestURI();
     *        String method = httpRequest.getMethod();
     *
     *        // 요청/응답 본문 캐싱을 위한 래퍼
     *        ContentCachingRequestWrapper requestWrapper =
     *            new ContentCachingRequestWrapper(httpRequest);
     *        ContentCachingResponseWrapper responseWrapper =
     *            new ContentCachingResponseWrapper((HttpServletResponse) response);
     *
     *        log.info("[LoggingFilter] ▶️ 요청 시작: [{}] {}", method, uri);
     *
     *        long start = System.currentTimeMillis();
     *        try {
     *            chain.doFilter(requestWrapper, responseWrapper);
     *        } finally {
     *            long end = System.currentTimeMillis();
     *
     *            // 요청 본문 로깅 (POST, PUT 등에서 유용)
     *            byte[] requestContent = requestWrapper.getContentAsByteArray();
     *            if (requestContent.length > 0 && isContentTypeLoggable(requestWrapper.getContentType())) {
     *                String requestBody = new String(requestContent, requestWrapper.getCharacterEncoding());
     *                log.debug("[LoggingFilter] 요청 본문: {}", requestBody);
     *            }
     *
     *            // 응답 본문 로깅
     *            byte[] responseContent = responseWrapper.getContentAsByteArray();
     *            if (responseContent.length > 0 && isContentTypeLoggable(responseWrapper.getContentType())) {
     *                String responseBody = new String(responseContent, responseWrapper.getCharacterEncoding());
     *                log.debug("[LoggingFilter] 응답 본문: {}", responseBody);
     *            }
     *
     *            // 응답 복사 (중요: 이 단계가 없으면 클라이언트는 빈 응답을 받음)
     *            responseWrapper.copyBodyToResponse();
     *
     *            log.info("[LoggingFilter] ⏹️ 응답 완료: [{}] {} - {}ms",
     *                     method, uri, (end - start));
     *        }
     *    }
     *
     *    private boolean isContentTypeLoggable(String contentType) {
     *        return contentType != null &&
     *               (contentType.startsWith("application/json") ||
     *                contentType.startsWith("application/xml") ||
     *                contentType.startsWith("text/"));
     *    }
     *
     * 2. MDC를 활용한 요청 추적:
     *    @Override
     *    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *            throws IOException, ServletException {
     *        HttpServletRequest httpRequest = (HttpServletRequest) request;
     *
     *        // 요청 ID 생성 및 MDC에 설정
     *        String requestId = UUID.randomUUID().toString().substring(0, 8);
     *        MDC.put("requestId", requestId);
     *
     *        // 사용자 정보 추가
     *        String userId = getUserIdFromRequest(httpRequest);
     *        if (userId != null) {
     *            MDC.put("userId", userId);
     *        }
     *
     *        try {
     *            log.info("[LoggingFilter] ▶️ 요청: [{}] {} from {}",
     *                     httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getRemoteAddr());
     *
     *            chain.doFilter(request, response);
     *
     *            log.info("[LoggingFilter] ⏹️ 응답 완료");
     *        } finally {
     *            // MDC 정리 (메모리 누수 방지)
     *            MDC.clear();
     *        }
     *    }
     *
     *    private String getUserIdFromRequest(HttpServletRequest request) {
     *        // 세션, 쿠키, 토큰 등에서 사용자 ID 추출 로직
     *        HttpSession session = request.getSession(false);
     *        if (session != null && session.getAttribute("userId") != null) {
     *            return session.getAttribute("userId").toString();
     *        }
     *        return null;
     *    }
     *
     * 3. 개발/프로덕션 환경에 따른 로깅 레벨 제어:
     *    @Override
     *    public void init(FilterConfig filterConfig) throws ServletException {
     *        // 환경별 로깅 설정
     *        String env = System.getProperty("spring.profiles.active", "dev");
     *        if ("dev".equals(env) || "local".equals(env)) {
     *            loggingEnabled = true;
     *            detailedLogging = true;
     *        } else if ("prod".equals(env)) {
     *            loggingEnabled = true;
     *            detailedLogging = false; // 프로덕션에서는 상세 로깅 비활성화
     *        }
     *
     *        log.info("[LoggingFilter] ▶️ 필터 초기화 완료: 로깅={}, 상세로깅={}",
     *                 loggingEnabled, detailedLogging);
     *    }
     *
     * 4. 헤더 정보 로깅:
     *    @Override
     *    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *            throws IOException, ServletException {
     *        HttpServletRequest httpRequest = (HttpServletRequest) request;
     *
     *        // 주요 헤더 로깅
     *        if (log.isDebugEnabled()) {
     *            Enumeration<String> headerNames = httpRequest.getHeaderNames();
     *            if (headerNames != null) {
     *                StringBuilder headers = new StringBuilder();
     *                while (headerNames.hasMoreElements()) {
     *                    String headerName = headerNames.nextElement();
     *                    // 민감한 정보(예: Authorization) 로깅 제외
     *                    if (!"Authorization".equalsIgnoreCase(headerName) &&
     *                        !"Cookie".equalsIgnoreCase(headerName)) {
     *                        headers.append(headerName).append(": ")
     *                               .append(httpRequest.getHeader(headerName)).append(", ");
     *                    }
     *                }
     *                log.debug("[LoggingFilter] 요청 헤더: {}", headers);
     *            }
     *        }
     *
     *        // 기본 로깅 및 필터 체인 진행
     *        log.info("[LoggingFilter] ▶️ 요청: [{}] {} from {}",
     *                 httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getRemoteAddr());
     *
     *        chain.doFilter(request, response);
     *
     *        log.info("[LoggingFilter] ⏹️ 응답 완료");
     *    }
     *
     * 5. 성능 모니터링:
     *    @Override
     *    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *            throws IOException, ServletException {
     *        HttpServletRequest httpRequest = (HttpServletRequest) request;
     *        String uri = httpRequest.getRequestURI();
     *
     *        long startTime = System.currentTimeMillis();
     *
     *        try {
     *            chain.doFilter(request, response);
     *        } finally {
     *            long duration = System.currentTimeMillis() - startTime;
     *
     *            // 임계값 초과 시 경고 로그
     *            if (duration > 1000) { // 1초 이상 걸리는 요청
     *                log.warn("[LoggingFilter] ⚠️ 성능 경고: [{}] {} - {}ms 소요",
     *                         httpRequest.getMethod(), uri, duration);
     *            } else {
     *                log.info("[LoggingFilter] ⏹️ 응답 완료: [{}] {} - {}ms",
     *                         httpRequest.getMethod(), uri, duration);
     *            }
     *
     *            // 통계 수집 코드 (외부 모니터링 시스템 연동 등)
     *            collectPerformanceMetrics(uri, httpRequest.getMethod(), duration);
     *        }
     *    }
     *
     *    private void collectPerformanceMetrics(String uri, String method, long duration) {
     *        // 메트릭 수집 로직 (Prometheus, Grafana 등 연동)
     *    }
     *
     * 🔧 흐름 테스트 방법:
     * 1. 요청 로깅 시각화:
     *    @Slf4j
     *    public class LoggingFilter implements Filter {
     *        @Override
     *        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
     *                throws IOException, ServletException {
     *            HttpServletRequest httpRequest = (HttpServletRequest) request;
     *
     *            log.info("\n===================================\n" +
     *                     "🔹 요청 시작: [{}] {}\n" +
     *                     "🔹 클라이언트: {}\n" +
     *                     "🔹 시간: {}\n" +
     *                     "===================================",
     *                     httpRequest.getMethod(),
     *                     httpRequest.getRequestURI(),
     *                     httpRequest.getRemoteAddr(),
     *                     LocalDateTime.now());
     *
     *            chain.doFilter(request, response);
     *
     *            log.info("\n===================================\n" +
     *                     "🔸 요청 완료: [{}] {}\n" +
     *                     "🔸 시간: {}\n" +
     *                     "===================================",
     *                     httpRequest.getMethod(),
     *                     httpRequest.getRequestURI(),
     *                     LocalDateTime.now());
     *        }
     *    }
     *
     * 2. 필터 체인 실행 순서 확인:
     *    - 로깅 필터, 보안 필터 등 여러 필터 등록 후 로그 순서 확인
     */

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("[LoggingFilter] ▶️ 필터 초기화 완료");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String clientIp = httpRequest.getRemoteAddr();

        log.info("[LoggingFilter] ▶️ 요청: [{}] {} from {}", method, uri, clientIp);

        chain.doFilter(request, response);

        log.info("[LoggingFilter] ⏹️ 응답 완료: [{}] {}", method, uri);
    }

    @Override
    public void destroy() {
        log.info("[LoggingFilter] ❌ 필터 종료");
    }
}