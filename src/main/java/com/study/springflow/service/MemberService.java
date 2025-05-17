package com.study.springflow.service;

import com.study.springflow.entity.Member;
import com.study.springflow.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    /**
     * 회원 서비스 클래스
     * - 비즈니스 로직 처리
     * - 트랜잭션 관리 (@Transactional)
     * - AOP 적용 테스트를 위한 대상
     */
    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 회원 가입 서비스
     * - 트랜잭션 관리 (실패 시 롤백)
     */
    @Transactional
    public Member register(Member member) {
        System.out.println("[MemberService] 회원 가입 시작: " + member.getUsername());

        // 중복 사용자 검증
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + member.getUsername());
        }

        // 중복 이메일 검증
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + member.getEmail());
        }

        // 회원 저장
        member.setCreatedAt(LocalDateTime.now());
        Member savedMember = memberRepository.save(member);

        System.out.println("[MemberService] 회원 가입 완료: " + savedMember.getId());
        return savedMember;
    }

    /**
     * 회원 조회 서비스 (읽기 전용 트랜잭션)
     */
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        System.out.println("[MemberService] ID로 회원 조회: " + id);
        return memberRepository.findById(id);
    }

    /**
     * 사용자명으로 회원 조회
     */
    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        System.out.println("[MemberService] 사용자명으로 회원 조회: " + username);
        return memberRepository.findByUsername(username);
    }

    /**
     * 관리자 역할 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Member> findAdmins() {
        System.out.println("[MemberService] 관리자 목록 조회");
        return memberRepository.findByRole(Member.MemberRole.ADMIN);
    }

    /**
     * 마지막 로그인 시간 업데이트
     * - 명시적 트랜잭션 관리 예시
     */
    @Transactional
    public Member updateLastLogin(Long memberId) {
        System.out.println("[MemberService] 로그인 시간 업데이트: " + memberId);

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
        System.out.println("[MemberService] 회원 삭제 시작: " + memberId);

        memberRepository.deleteById(memberId);

        // 트랜잭션 롤백 테스트를 위한 의도적 예외 발생
        if (simulateError) {
            System.out.println("[MemberService] 의도적 예외 발생 (트랜잭션 롤백 테스트)");
            throw new RuntimeException("회원 삭제 중 의도적 오류 발생 (롤백 테스트)");
        }

        System.out.println("[MemberService] 회원 삭제 완료: " + memberId);
    }
}