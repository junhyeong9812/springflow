package com.study.springflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    /**
     * 간단한 테스트용 컨트롤러
     * - 요청 흐름 테스트를 위한 엔드포인트 제공
     */
    @GetMapping("/hello")
    public String hello() {
        System.out.println("[HelloController] hello() 메서드 실행");
        return "Hello, SpringFlow!";
    }

    /**
     * 예외 처리 테스트용 엔드포인트
     */
    @GetMapping("/error-test")
    public String errorTest() {
        System.out.println("[HelloController] errorTest() 메서드 실행");
        throw new RuntimeException("테스트 예외 발생");
    }
}