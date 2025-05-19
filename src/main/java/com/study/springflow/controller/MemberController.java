package com.study.springflow.controller;

import com.study.springflow.entity.Member;
import com.study.springflow.security.dto.PasswordChangeRequest;
import com.study.springflow.service.MemberService;
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
 * 회원 관리 REST 컨트롤러 (시큐리티 적용)
 * - HTTP 요청 처리 및 서비스 연동
 * - Spring Security의 @PreAuthorize를 사용한 메서드 레벨 보안
 * - @AuthenticationPrincipal을 통한 현재 로그인한 사용자 정보 접근
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 상세 조회 API (본인 또는 관리자만 접근 가능)
     * - 메서드 레벨 보안 적용
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @memberSecurity.isResourceOwner(#id, authentication)")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
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
    public ResponseEntity<Member> getMemberByUsername(@PathVariable String username) {
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
    public ResponseEntity<List<Member>> getAdmins() {
        log.info("[MemberController] 관리자 목록 조회 요청");
        List<Member> admins = memberService.findAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * 로그인 시간 업데이트 API
     * - AuthController에서 로그인 시 호출하므로 별도 인증 불필요
     */
    @PutMapping("/{id}/login")
    public ResponseEntity<Member> updateLastLogin(@PathVariable Long id) {
        log.info("[MemberController] 마지막 로그인 시간 업데이트: {}", id);
        Member updatedMember = memberService.updateLastLogin(id);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * 비밀번호 변경 API (본인만 접근 가능)
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("@memberSecurity.isResourceOwner(#id, authentication)")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
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
    public ResponseEntity<Map<String, String>> deleteMember(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean simulateError) {

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