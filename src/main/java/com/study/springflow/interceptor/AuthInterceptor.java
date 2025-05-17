package com.study.springflow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    /**
     * ✅ 인증 인터셉터
     * - 요청이 컨트롤러에 도달하기 전 사전 처리 담당
     * - Filter와 달리 Spring MVC 컴포넌트로, 스프링 빈 주입 및 스프링 기능 사용 가능
     * - 다양한 시점(preHandle, postHandle, afterCompletion)에서 로직 수행 가능
     *
     * 🔍 추가 활용 옵션:
     * 1. 인증 처리 로직 구현:
     *    @Override
     *    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
     *            throws Exception {
     *        // 1. 인증 제외 경로 확인
     *        String uri = request.getRequestURI();
     *        if (isExcludedPath(uri)) {
     *            return true; // 인증 검사 없이 통과
     *        }
     *
     *        // 2. 인증 토큰 확인
     *        String token = extractToken(request);
     *        if (token == null) {
     *            // 인증 정보 없음 - 401 Unauthorized
     *            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
     *            response.setContentType("application/json");
     *            response.getWriter().write("{\"error\": \"인증 정보가 필요합니다.\"}");
     *            return false; // 컨트롤러 진행 중단
     *        }
     *
     *        // 3. 토큰 검증
     *        if (!isValidToken(token)) {
     *            // 유효하지 않은 토큰 - 403 Forbidden
     *            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
     *            response.setContentType("application/json");
     *            response.getWriter().write("{\"error\": \"유효하지 않은 인증 정보입니다.\"}");
     *            return false; // 컨트롤러 진행 중단
     *        }
     *
     *        // 4. 사용자 정보 설정
     *        UserDetails userDetails = getUserFromToken(token);
     *        request.setAttribute("currentUser", userDetails);
     *
     *        return true; // 컨트롤러로 진행
     *    }
     *
     *    private boolean isExcludedPath(String uri) {
     *        return uri.startsWith("/public/") ||
     *               uri.startsWith("/auth/") ||
     *               uri.equals("/login") ||
     *               uri.equals("/signup");
     *    }
     *
     *    private String extractToken(HttpServletRequest request) {
     *        String authHeader = request.getHeader("Authorization");
     *        if (authHeader != null && authHeader.startsWith("Bearer ")) {
     *            return authHeader.substring(7); // "Bearer " 제거
     *        }
     *        return null;
     *    }
     *
     * 2. 권한 검사 확장:
     *    @Override
     *    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
     *            throws Exception {
     *        // 기본 인증 처리 생략...
     *
     *        // 핸들러가 메서드 핸들러인 경우만 처리
     *        if (handler instanceof HandlerMethod) {
     *            HandlerMethod handlerMethod = (HandlerMethod) handler;
     *
     *            // @AdminOnly 어노테이션 확인
     *            AdminOnly adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class);
     *            if (adminOnly != null) {
     *                UserDetails user = (UserDetails) request.getAttribute("currentUser");
     *                if (user == null || !user.hasRole("ADMIN")) {
     *                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
     *                    response.getWriter().write("관리자 권한이 필요합니다.");
     *                    return false;
     *                }
     *            }
     *
     *            // @RequiredPermission 어노테이션 확인
     *            RequiredPermission requiredPermission = handlerMethod.getMethodAnnotation(RequiredPermission.class);
     *            if (requiredPermission != null) {
     *                UserDetails user = (UserDetails) request.getAttribute("currentUser");
     *                String[] permissions = requiredPermission.value();
     *
     *                if (user == null || !hasAnyPermission(user, permissions)) {
     *                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
     *                    response.getWriter().write("필요한 권한이 없습니다.");
     *                    return false;
     *                }
     *            }
     *        }
     *
     *        return true;
     *    }
     *
     * 3. 컨트롤러 호출 후 처리:
     *    @Override
     *    public void postHandle(HttpServletRequest request, HttpServletResponse response,
     *                           Object handler, ModelAndView modelAndView) throws Exception {
     *        // 뷰 렌더링 전, 컨트롤러 실행 후 호출
     *        // ModelAndView 조작 가능
     *
     *        if (modelAndView != null) {
     *            // 공통 데이터 추가
     *            UserDetails user = (UserDetails) request.getAttribute("currentUser");
     *            if (user != null) {
     *                modelAndView.addObject("user", user);
     *            }
     *
     *            // 권한별 메뉴 설정
     *            modelAndView.addObject("menus", getMenusForUser(user));
     *
     *            // 글로벌 설정 주입
     *            modelAndView.addObject("appVersion", "1.0.0");
     *            modelAndView.addObject("environment", "development");
     *        }
     *    }
     *
     * 4. 예외 처리와 리소스 정리:
     *    @Override
     *    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
     *                               Object handler, Exception ex) throws Exception {
     *        // 뷰 렌더링 후 호출, 예외가 발생해도 호출됨
     *
     *        // 요청 처리 시간 기록
     *        Long startTime = (Long) request.getAttribute("startTime");
     *        if (startTime != null) {
     *            long duration = System.currentTimeMillis() - startTime;
     *            System.out.println("요청 처리 시간: " + duration + "ms");
     *
     *            // 임계값 초과 시 로깅
     *            if (duration > 1000) {
     *                System.out.println("성능 경고: " + request.getRequestURI() + " - " + duration + "ms");
     *            }
     *        }
     *
     *        // 오류 발생 시 처리
     *        if (ex != null) {
     *            System.err.println("요청 처리 중 오류 발생: " + ex.getMessage());
     *        }
     *
     *        // 임시 리소스 정리
     *        cleanupResources(request);
     *    }
     *
     * 5. 로깅 및 감사(Auditing) 확장:
     *    @Override
     *    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
     *            throws Exception {
     *        // 요청 시작 시간 기록
     *        request.setAttribute("startTime", System.currentTimeMillis());
     *
     *        // 요청 정보 로깅
     *        String requestId = UUID.randomUUID().toString();
     *        request.setAttribute("requestId", requestId);
     *
     *        UserDetails user = getCurrentUser(request);
     *        String userId = user != null ? user.getId() : "anonymous";
     *
     *        // 감사 로그 기록
     *        AuditLog auditLog = new AuditLog();
     *        auditLog.setRequestId(requestId);
     *        auditLog.setUserId(userId);
     *        auditLog.setUri(request.getRequestURI());
     *        auditLog.setMethod(request.getMethod());
     *        auditLog.setTimestamp(LocalDateTime.now());
     *        auditLog.setIpAddress(request.getRemoteAddr());
     *        auditLog.setUserAgent(request.getHeader("User-Agent"));
     *
     *        // 감사 로그 저장 (비동기)
     *        auditLogService.saveAsync(auditLog);
     *
     *        return true;
     *    }
     *
     *    @Override
     *    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
     *                               Object handler, Exception ex) throws Exception {
     *        // 요청 완료 시간 및 상태 업데이트
     *        String requestId = (String) request.getAttribute("requestId");
     *        if (requestId != null) {
     *            Long startTime = (Long) request.getAttribute("startTime");
     *            long duration = System.currentTimeMillis() - startTime;
     *
     *            // 감사 로그 업데이트
     *            auditLogService.updateAuditLog(
     *                requestId,
     *                response.getStatus(),
     *                duration,
     *                ex != null ? ex.getMessage() : null
     *            );
     *        }
     *    }
     *
     * 🔧 흐름 테스트 방법:
     * 1. 인터셉터 실행 순서 확인:
     *    @Component
     *    @Order(1) // 낮은 숫자가 먼저 실행
     *    public class LoggingInterceptor implements HandlerInterceptor {
     *        @Override
     *        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
     *            System.out.println("1. LoggingInterceptor.preHandle 실행");
     *            return true;
     *        }
     *
     *        @Override
     *        public void postHandle(HttpServletRequest request, HttpServletResponse response,
     *                              Object handler, ModelAndView modelAndView) {
     *            System.out.println("4. LoggingInterceptor.postHandle 실행");
     *        }
     *
     *        @Override
     *        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
     *                                   Object handler, Exception ex) {
     *            System.out.println("6. LoggingInterceptor.afterCompletion 실행");
     *        }
     *    }
     *
     *    @Component
     *    @Order(2)
     *    public class AuthInterceptor implements HandlerInterceptor {
     *        @Override
     *        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
     *            System.out.println("2. AuthInterceptor.preHandle 실행");
     *            return true;
     *        }
     *
     *        @Override
     *        public void postHandle(HttpServletRequest request, HttpServletResponse response,
     *                              Object handler, ModelAndView modelAndView) {
     *            System.out.println("5. AuthInterceptor.postHandle 실행");
     *        }
     *
     *        @Override
     *        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
     *                                   Object handler, Exception ex) {
     *            System.out.println("7. AuthInterceptor.afterCompletion 실행");
     *        }
     *    }
     *
     *    // 컨트롤러에 로그 추가:
     *    @GetMapping("/test")
     *    public String test() {
     *        System.out.println("3. 컨트롤러 메서드 실행");
     *        return "test";
     *    }
     *
     *    // 실행 순서:
     *    // 1. LoggingInterceptor.preHandle 실행
     *    // 2. AuthInterceptor.preHandle 실행
     *    // 3. 컨트롤러 메서드 실행
     *    // 4. AuthInterceptor.postHandle 실행
     *    // 5. LoggingInterceptor.postHandle 실행
     *    // 6. AuthInterceptor.afterCompletion 실행
     *    // 7. LoggingInterceptor.afterCompletion 실행
     */

    // ✅ 요청 전 처리
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("[AuthInterceptor] 요청 URL: " + request.getRequestURI());
        return true; // false일 경우 컨트롤러로 요청이 전달되지 않음
    }

    // 필요 시 postHandle, afterCompletion도 구현 가능
}