package com.study.springflow.repository;

import com.study.springflow.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 회원 리포지토리 인터페이스
     * - Spring Data JPA를 활용한 데이터 액세스 레이어
     * - 메서드 이름 기반 쿼리, JPQL 쿼리 예시 포함
     */

    // 사용자명으로 회원 찾기 (메서드 이름 기반 쿼리)
    Optional<Member> findByUsername(String username);

    // 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 역할별 회원 목록 조회
    List<Member> findByRole(Member.MemberRole role);

    // JPQL을 사용한 커스텀 쿼리
    @Query("SELECT m FROM Member m WHERE m.role = :role ORDER BY m.createdAt DESC")
    List<Member> findMembersByRoleOrderByCreatedAtDesc(@Param("role") Member.MemberRole role);

    // 네이티브 SQL 쿼리 사용 예시
    @Query(value = "SELECT * FROM members WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    List<Member> searchMembersByNameIgnoreCase(@Param("keyword") String keyword);
}