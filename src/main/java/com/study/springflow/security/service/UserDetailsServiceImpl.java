package com.study.springflow.security.service;

import com.study.springflow.entity.Member;
import com.study.springflow.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 스프링 시큐리티 사용자 상세 서비스 구현
 * - 데이터베이스에서 사용자 정보를 조회하여 인증에 사용
 * - UserDetailsService 인터페이스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 사용자명으로 사용자 상세 정보 조회
     * @param username 사용자명
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[UserDetailsService] 사용자 인증 정보 조회: {}", username);

        return memberRepository.findByUsername(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> {
                    log.error("[UserDetailsService] 사용자를 찾을 수 없음: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });
    }

    /**
     * Member 엔티티를 UserDetails 객체로 변환
     * @param member 회원 엔티티
     * @return UserDetails 객체
     */
    private UserDetails createUserDetails(Member member) {
        // 권한 정보 생성 - "ROLE_" 접두사 필요
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + member.getRole().name());

        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .authorities(Collections.singleton(authority))
                .build();
    }
}