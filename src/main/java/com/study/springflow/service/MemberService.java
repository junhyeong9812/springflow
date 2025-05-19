package com.study.springflow.service;

import com.study.springflow.entity.Member;
import com.study.springflow.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 회원 서비스 클래스 (시큐리티 기능 추가)
 * - 비즈니스 로직 처리
 * - 트랜잭션 관리 (@Transactional)
 * - 비밀번호 암호화 기능 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 인코더 추가

    /**
     * 회원 가입 서비스
     * - 트랜잭션 관리 (실패 시 롤백)
     * - 비밀번호 암호화 처리
     */
    @Transactional
    public Member register(Member member) {
        log.info("[MemberService] 회원 가입 시작: {}", member.getUsername());

        // 중복 사용자 검증
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + member.getUsername());
        }

        // 중복 이메일 검증
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + member.getEmail());
        }

        // 비밀번호 암호화 처리
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // 회원 저장
        member.setCreatedAt(LocalDateTime.now());
        Member savedMember = memberRepository.save(member);

        log.info("[MemberService] 회원 가입 완료: {}", savedMember.getId());
        return savedMember;
    }

    /**
     * 회원 조회 서비스 (읽기 전용 트랜잭션)
     */
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        log.info("[MemberService] ID로 회원 조회: {}", id);
        return memberRepository.findById(id);
    }

    /**
     * 사용자명으로 회원 조회
     */
    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        log.info("[MemberService] 사용자명으로 회원 조회: {}", username);
        return memberRepository.findByUsername(username);
    }

    /**
     * 관리자 역할 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Member> findAdmins() {
        log.info("[MemberService] 관리자 목록 조회");
        return memberRepository.findByRole(Member.MemberRole.ADMIN);
    }

    /**
     * 마지막 로그인 시간 업데이트
     * - 명시적 트랜잭션 관리 예시
     */
    @Transactional
    public Member updateLastLogin(Long memberId) {
        log.info("[MemberService] 로그인 시간 업데이트: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        member.setLastLoginAt(LocalDateTime.now());
        return memberRepository.save(member);
    }

    /**
     * 회원 정보 삭제
     * - 트랜잭션 롤백 테스트를 위한 예시 (의도적 예외 발생)
     */
    @Transactional
    public void delete(Long memberId, boolean simulateError) {
        log.info("[MemberService] 회원 삭제 시작: {}", memberId);

        memberRepository.deleteById(memberId);

        // 트랜잭션 롤백 테스트를 위한 의도적 예외 발생
        if (simulateError) {
            log.info("[MemberService] 의도적 예외 발생 (트랜잭션 롤백 테스트)");
            throw new RuntimeException("회원 삭제 중 의도적 오류 발생 (롤백 테스트)");
        }

        log.info("[MemberService] 회원 삭제 완료: {}", memberId);
    }

    /**
     * 회원 비밀번호 변경
     * - 기존 비밀번호 확인 후 새 비밀번호로 변경
     */
    @Transactional
    public Member changePassword(Long memberId, String currentPassword, String newPassword) {
        log.info("[MemberService] 비밀번호 변경 시작: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        member.setPassword(passwordEncoder.encode(newPassword));
        Member updatedMember = memberRepository.save(member);

        log.info("[MemberService] 비밀번호 변경 완료: {}", memberId);
        return updatedMember;
    }
}