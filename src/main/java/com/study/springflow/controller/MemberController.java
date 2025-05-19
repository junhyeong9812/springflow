package com.study.springflow.controller;

import com.study.springflow.entity.Member;
import com.study.springflow.security.dto.PasswordChangeRequest;
import com.study.springflow.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 회원 관리 REST 컨트롤러 (시큐리티 적용, Swagger 문서화)
 * - HTTP 요청 처리 및 서비스 연동
 * - Spring Security의 @PreAuthorize를 사용한 메서드 레벨 보안
 * - @AuthenticationPrincipal을 통한 현재 로그인한 사용자 정보 접근
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원 관리", description = "회원 정보 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 상세 조회 API (본인 또는 관리자만 접근 가능)
     * - 메서드 레벨 보안 적용
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @memberSecurity.isResourceOwner(#id, authentication)")
    @Operation(
            summary = "회원 상세 조회",
            description = "회원 ID로 상세 정보 조회 (본인 또는 관리자만 접근 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ResponseEntity<Member> getMember(
            @Parameter(description = "회원 ID") @PathVariable Long id) {
        log.info("[MemberController] 회원 조회 요청: {}", id);
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API
     * - @AuthenticationPrincipal 사용 예시
     */
    @GetMapping("/me")
    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자 정보 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ResponseEntity<Member> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("[MemberController] 현재 로그인 사용자 정보 조회: {}", userDetails.getUsername());

        return memberService.findByUsername(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자명으로 회원 조회 API (관리자만 접근 가능)
     */
    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "사용자명으로 회원 조회",
            description = "사용자명으로 회원 정보 조회 (관리자만 접근 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ResponseEntity<Member> getMemberByUsername(
            @Parameter(description = "사용자명") @PathVariable String username) {
        log.info("[MemberController] 사용자명으로 회원 조회: {}", username);

        return memberService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 관리자 목록 조회 API (관리자만 접근 가능)
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "관리자 목록 조회",
            description = "관리자 권한을 가진 회원 목록 조회 (관리자만 접근 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<List<Member>> getAdmins() {
        log.info("[MemberController] 관리자 목록 조회 요청");
        List<Member> admins = memberService.findAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * 비밀번호 변경 API (본인만 접근 가능)
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("@memberSecurity.isResourceOwner(#id, authentication)")
    @Operation(
            summary = "비밀번호 변경",
            description = "회원 비밀번호 변경 (본인만 접근 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(description = "회원 ID") @PathVariable Long id,
            @RequestBody PasswordChangeRequest request) {

        log.info("[MemberController] 비밀번호 변경 요청: {}", id);

        try {
            memberService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 회원 삭제 API (본인 또는 관리자만 접근 가능)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @memberSecurity.isResourceOwner(#id, authentication)")
    @Operation(
            summary = "회원 삭제",
            description = "회원 삭제 (본인 또는 관리자만 접근 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, String>> deleteMember(
            @Parameter(description = "회원 ID") @PathVariable Long id,
            @Parameter(description = "오류 시뮬레이션 여부") @RequestParam(defaultValue = "false") boolean simulateError) {

        log.info("[MemberController] 회원 삭제 요청: {}" +
                (simulateError ? " (오류 시뮬레이션)" : ""), id);

        try {
            memberService.delete(id, simulateError);
            return ResponseEntity.ok(Map.of("message", "회원이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}