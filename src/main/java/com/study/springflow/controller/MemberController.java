package com.study.springflow.controller;

import com.study.springflow.entity.Member;
import com.study.springflow.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    /**
     * 회원 관리 REST 컨트롤러
     * - HTTP 요청 처리 및 서비스 연동
     * - 요청 흐름 추적을 위한 API 엔드포인트 제공
     */
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 회원 등록 API
     */
    @PostMapping
    public ResponseEntity<Member> registerMember(@RequestBody Member member) {
        System.out.println("[MemberController] 회원 등록 요청: " + member.getUsername());
        Member registeredMember = memberService.register(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredMember);
    }

    /**
     * 회원 상세 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        System.out.println("[MemberController] 회원 조회 요청: " + id);
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자명으로 회원 조회 API
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<Member> getMemberByUsername(@PathVariable String username) {
        System.out.println("[MemberController] 사용자명으로 회원 조회: " + username);
        return memberService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 관리자 목록 조회 API
     */
    @GetMapping("/admins")
    public ResponseEntity<List<Member>> getAdmins() {
        System.out.println("[MemberController] 관리자 목록 조회 요청");
        List<Member> admins = memberService.findAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * 로그인 시간 업데이트 API
     */
    @PutMapping("/{id}/login")
    public ResponseEntity<Member> updateLastLogin(@PathVariable Long id) {
        System.out.println("[MemberController] 마지막 로그인 시간 업데이트: " + id);
        Member updatedMember = memberService.updateLastLogin(id);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * 회원 삭제 API (트랜잭션 롤백 테스트 포함)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMember(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "false") boolean simulateError) {
        System.out.println("[MemberController] 회원 삭제 요청: " + id +
                (simulateError ? " (오류 시뮬레이션)" : ""));

        try {
            memberService.delete(id, simulateError);
            return ResponseEntity.ok(Map.of("message", "회원이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}