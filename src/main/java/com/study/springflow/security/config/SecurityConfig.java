package com.study.springflow.security.config;

import com.study.springflow.security.jwt.JwtAccessDeniedHandler;
import com.study.springflow.security.jwt.JwtAuthenticationEntryPoint;
import com.study.springflow.security.jwt.JwtAuthenticationFilter;
import com.study.springflow.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 스프링 시큐리티 설정 클래스 (Swagger 지원 추가)
 * - 보안 필터 체인 구성
 * - 인증/인가 규칙 설정
 * - JWT 관련 설정
 * - Swagger UI 접근 허용
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 패스워드 인코더 빈 등록
     * - BCrypt 알고리즘 사용 (Spring Security 권장)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 관리자 빈 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 설정
     * - Swagger UI 접근 허용 추가
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("[SecurityConfig] 보안 필터 체인 구성");

        http
                // CSRF 보호 비활성화 (REST API 서버는 상태를 유지하지 않음)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정 사용
                .cors(cors -> {})

                // 세션 사용 안함 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 예외 처리
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패 처리
                                .accessDeniedHandler(jwtAccessDeniedHandler) // 인가 실패 처리
                )

                // 요청 권한 설정
                .authorizeHttpRequests(authorize ->
                        authorize
                                // 인증 없이 접근 가능한 경로
                                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                                .requestMatchers("/", "/hello").permitAll()
                                .requestMatchers("/error/**").permitAll()

                                // Swagger UI 관련 경로 허용
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                                // 관리자 권한이 필요한 경로
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                // 그 외 요청은 인증 필요
                                .anyRequest().authenticated()
                )

                // H2 콘솔 사용을 위한 설정
                .headers(headers ->
                        headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}