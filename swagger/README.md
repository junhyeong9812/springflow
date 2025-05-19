# Swagger UI를 이용한 API 테스트 가이드

SpringFlow 프로젝트에 Swagger UI가 통합되어 API 문서화 및 테스트가 가능합니다. 이 가이드는 Swagger UI를 사용하여 JWT 인증이 적용된 API를 테스트하는 방법을 설명합니다.

## Swagger UI 접근 방법

애플리케이션 실행 후, 브라우저에서 아래 URL로 접근하세요:
```
http://localhost:8080/swagger-ui/index.html
```

## API 목록

Swagger UI에서는 다음과 같은 API 그룹을 확인할 수 있습니다:

1. **인증 API**: 로그인, 회원가입
2. **회원 관리 API**: 회원 정보 조회, 비밀번호 변경, 회원 삭제 등

## JWT 인증 테스트 방법

JWT 인증이 필요한 API를 테스트하려면 다음 단계를 따르세요:

### 1. 계정 생성 또는 로그인

1. **회원가입 API** 사용 (계정이 없는 경우):
    - `/api/auth/register` 엔드포인트를 찾아 "Try it out" 클릭
    - 요청 본문에 필요한 정보 입력:
      ```json
      {
        "username": "testuser",
        "password": "password123",
        "name": "테스트 사용자",
        "email": "test@example.com",
        "role": "USER"
      }
      ```
    - "Execute" 버튼 클릭하여 요청 전송

2. **로그인 API** 사용:
    - `/api/auth/login` 엔드포인트를 찾아 "Try it out" 클릭
    - 요청 본문에 자격 증명 입력:
      ```json
      {
        "username": "testuser",
        "password": "password123"
      }
      ```
    - "Execute" 버튼 클릭하여 요청 전송
    - 응답에서 `token` 값 복사 (JWT 토큰)

### 2. JWT 토큰 설정

1. Swagger UI 화면 우측 상단에 **Authorize** 버튼 클릭
2. 표시되는 팝업에서 `bearerAuth` 섹션 아래에 `Bearer {복사한_토큰}` 형식으로 입력
    - `Bearer ` 접두사를 포함하여 입력해야 함 (예: `Bearer eyJhbGciOiJIUzI1NiJ9...`)
3. **Authorize** 버튼 클릭
4. 창을 닫으면 이제 인증이 필요한 모든 API 요청에 자동으로 토큰이 포함됩니다

### 3. 인증이 필요한 API 테스트

이제 다음과 같은 보호된 API를 테스트할 수 있습니다:

1. **내 정보 조회**:
    - `/api/members/me` 엔드포인트 사용

2. **비밀번호 변경**:
    - `/api/members/{id}/password` 엔드포인트 사용
    - 요청 본문:
      ```json
      {
        "currentPassword": "password123",
        "newPassword": "newpassword123"
      }
      ```

3. **회원 삭제**:
    - `/api/members/{id}` 엔드포인트의 DELETE 메서드 사용

## 권한 테스트

서로 다른 역할(USER, ADMIN)로 로그인하여 권한 기반 접근 제어를 테스트할 수 있습니다:

1. 일반 사용자(USER)로 로그인 후:
    - `/api/members/admins`와 같은 관리자 전용 API에 접근 시 403 Forbidden 응답 확인

2. 관리자(ADMIN)로 로그인 후:
    - 모든 API에 접근 가능함을 확인
    - 다른 사용자의 정보 조회/수정 가능함을 확인

## 토큰 만료 테스트

JWT 토큰 만료 동작을 테스트하려면:

1. 로그인하여 토큰 획득
2. 토큰의 유효 시간이 지난 후 (기본 1시간) API 요청 시도
3. 401 Unauthorized 응답 확인
4. 다시 로그인하여 새 토큰 획득 후 요청 재시도

## 리소스 소유자 권한 테스트

1. 사용자 A로 로그인하여 토큰 획득
2. 사용자 B의 리소스(회원 정보 등)에 접근 시도:
    - `/api/members/{사용자B의_ID}` 조회 시 403 Forbidden 응답 확인
    - `/api/members/{사용자B의_ID}/password` 변경 시도 시 403 Forbidden 응답 확인

## 트러블슈팅

1. **인증 오류 (401 Unauthorized)**:
    - 토큰이 만료되었을 수 있음 → 다시 로그인
    - Authorize 팝업에서 토큰 형식이 올바른지 확인 (`Bearer ` 접두사 포함)

2. **권한 오류 (403 Forbidden)**:
    - 해당 리소스에 접근할 권한이 없음
    - 권한이 필요한 작업(관리자 전용 API 등)인지 확인

3. **요청 형식 오류 (400 Bad Request)**:
    - 요청 본문 형식이 올바른지 확인
    - 필수 필드가 누락되지 않았는지 확인

## API 설계 확인

Swagger UI를 통해 전체 API 목록과 각 API의 요청/응답 스키마, 권한 요구사항 등을 확인할 수 있습니다. 이는 개발 및 테스트 과정에서 API 사용법을 이해하는 데 도움이 됩니다.