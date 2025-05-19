package com.study.springflow.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 요청마다 JWT 토큰을 검증하고 인증 정보를 설정
 * - Spring Security 필터 체인에 추가되어 인증 과정을 처리
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * HTTP 요청에서 JWT 토큰을 확인하고 인증 처리
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);
        String requestURI = request.getRequestURI();

        log.debug("[JwtAuthenticationFilter] URI: {}, JWT 토큰 존재 여부: {}", requestURI, (token != null));

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효하면 인증 정보 설정
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("[JwtAuthenticationFilter] '{}' 사용자 인증 성공", auth.getName());
        }

        filterChain.doFilter(request, response);
    }
}