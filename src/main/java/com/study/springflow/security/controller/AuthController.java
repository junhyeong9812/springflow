package com.study.springflow.security.controller;

import com.study.springflow.entity.Member;
import com.study.springflow.security.dto.LoginRequest;
import com.study.springflow.security.dto.TokenResponse;
import com.study.springflow.security.jwt.JwtTokenProvider;
import com.study.springflow.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API 컨트롤러
 * - 로그인, 회원가입 등 인증 관련 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API - 로그인, 회원가입")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    /**
     * 로그인 API
     * - 사용자 인증 후 JWT 토큰 발급
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("[AuthController] 로그인 요청: {}", loginRequest.getUsername());

        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 회원 정보 조회
        Member member = memberService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginRequest.getUsername()));

        // 로그인 시간 업데이트
        memberService.updateLastLogin(member.getId());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(member.getUsername(), member.getRole().name());

        // 응답 생성
        TokenResponse tokenResponse = TokenResponse.builder()
                .token(token)
                .username(member.getUsername())
                .role(member.getRole().name())
                .build();

        log.info("[AuthController] 로그인 성공: {}", member.getUsername());
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 회원가입 API
     * - 새 회원 등록
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새 회원 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = Member.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 중복된 사용자")
    })
    public ResponseEntity<Member> register(@RequestBody Member member) {
        log.info("[AuthController] 회원가입 요청: {}", member.getUsername());

        // 회원 등록 (MemberService에서 비밀번호 암호화)
        Member registeredMember = memberService.register(member);

        log.info("[AuthController] 회원가입 성공: {}", registeredMember.getUsername());
        return ResponseEntity.ok(registeredMember);
    }
}