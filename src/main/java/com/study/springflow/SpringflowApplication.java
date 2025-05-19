package com.study.springflow;

import com.study.springflow.entity.Member;
import com.study.springflow.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class SpringflowApplication {

	/**
	 * 애플리케이션 메인 클래스
	 * - Spring 구동 시작점
	 * - 프로젝트 패키지 스캔 기준점
	 */
	public static void main(String[] args) {
		log.info("\n=== SpringFlow 애플리케이션 시작 ===\n");
		SpringApplication.run(SpringflowApplication.class, args);
	}

	/**
	 * 애플리케이션 구동 시 초기 데이터 설정
	 * - 테스트를 위한 관리자/사용자 계정 생성
	 * - 비밀번호 암호화 적용
	 */
	@Bean
	public CommandLineRunner initData(MemberService memberService) {
		return args -> {
			log.info("\n=== 초기 데이터 설정 시작 ===\n");

			// 관리자 계정 생성
			try {
				Member admin = Member.builder()
						.username("admin")
						.password("admin123") // MemberService에서 암호화됨
						.name("관리자")
						.email("admin@example.com")
						.role(Member.MemberRole.ADMIN)
						.build();

				memberService.register(admin);
				log.info("관리자 계정 생성 완료: {}", admin.getUsername());
			} catch (Exception e) {
				log.error("관리자 계정 생성 실패: {}", e.getMessage());
			}

			// 일반 사용자 계정 생성
			try {
				Member user = Member.builder()
						.username("user")
						.password("user123") // MemberService에서 암호화됨
						.name("일반 사용자")
						.email("user@example.com")
						.role(Member.MemberRole.USER)
						.build();

				memberService.register(user);
				log.info("사용자 계정 생성 완료: {}", user.getUsername());
			} catch (Exception e) {
				log.error("사용자 계정 생성 실패: {}", e.getMessage());
			}

			log.info("\n=== 초기 데이터 설정 완료 ===\n");
		};
	}
}