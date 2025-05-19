package com.study.springflow.security.util;

import com.study.springflow.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 리소스 소유자 확인 등 보안 관련 유틸리티
 * - @PreAuthorize에서 SpEL 표현식으로 사용 가능
 */
@Slf4j
@Component("memberSecurity")
@RequiredArgsConstructor
public class MemberSecurityUtil {

    private final MemberService memberService;

    /**
     * 현재 인증된 사용자가 리소스 소유자인지 확인
     * @param resourceId 리소스 ID (회원 ID 등)
     * @param authentication 현재 인증 정보
     * @return 리소스 소유자 여부
     */
    public boolean isResourceOwner(Long resourceId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 현재 인증된 사용자 정보 추출
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        log.debug("[MemberSecurity] 리소스({}) 소유자 확인: {}", resourceId, username);

        // 사용자명으로 회원 ID 조회 후 비교
        return memberService.findByUsername(username)
                .map(member -> member.getId().equals(resourceId))
                .orElse(false);
    }
}