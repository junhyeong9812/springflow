# SpringFlow 보안 기능 테스트 안내

## 보안 기능 요약

1. **JWT 기반 인증**: 클라이언트는 사용자 인증 후 JWT 토큰을 발급받아 요청 시 사용합니다.
2. **ROLE 기반 권한 부여**: 각 엔드포인트는 ADMIN, USER 등 특정 권한이 필요합니다.
3. **리소스 소유자 확인**: 자신의 정보만 수정 가능하도록 제한됩니다(관리자 제외).
4. **비밀번호 암호화**: 사용자 비밀번호는 BCrypt로 암호화되어 저장됩니다.

## API 엔드포인트

### 인증 API (인증 불필요)

1. **회원가입**: `POST /api/auth/register`
   ```json
   {
     "username": "testuser",
     "password": "password123",
     "name": "테스트 사용자",
     "email": "test@example.com",
     "role": "USER"
   }
   ```

2. **로그인**: `POST /api/auth/login`
   ```json
   {
     "username": "testuser",
     "password": "password123"
   }
   ```
   응답:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "username": "testuser",
     "role": "USER"
   }
   ```

### 회원 관리 API (인증 필요)

**공통 요청 헤더**: 모든 API 요청에 아래 헤더 추가
```
Authorization: Bearer 발급받은_JWT_토큰
```

1. **현재 회원 정보**: `GET /api/members/me`

2. **회원 상세 조회**: `GET /api/members/{id}`
    - 자신의 ID 또는 관리자만 접근 가능

3. **사용자명으로 회원 조회**: `GET /api/members/by-username/{username}`
    - 관리자만 접근 가능

4. **관리자 목록 조회**: `GET /api/members/admins`
    - 관리자만 접근 가능

5. **비밀번호 변경**: `PUT /api/members/{id}/password`
    - 자신의 ID만 접근 가능
   ```json
   {
     "currentPassword": "현재_비밀번호",
     "newPassword": "새_비밀번호"
   }
   ```

6. **회원 삭제**: `DELETE /api/members/{id}`
    - 자신의 ID 또는 관리자만 접근 가능

## Postman을 이용한 테스트 방법

1. **환경 변수 설정**:
    - `BASE_URL`: `http://localhost:8080`
    - `TOKEN`: (로그인 후 자동 설정)

2. **로그인 후 토큰 자동 저장**:
   ```javascript
   // Tests 탭에 아래 스크립트 추가
   var response = pm.response.json();
   if (response.token) {
       pm.environment.set("TOKEN", response.token);
   }
   ```

3. **인증 헤더 설정**:
    - 인증이 필요한 요청의 Authorization 탭에서:
    - Type: Bearer Token
    - Token: `{{TOKEN}}`

4. **요청 순서**:
    1. 회원가입
    2. 로그인 (토큰 획득)
    3. 인증이 필요한 API 테스트

## 보안 기능 테스트 시나리오

1. **권한 기반 접근 테스트**:
    - 일반 사용자로 로그인 후 관리자 전용 API 호출 시 403 에러 확인
    - 관리자로 로그인 후 동일 API 호출 시 성공 확인

2. **리소스 소유자 확인 테스트**:
    - 사용자 A로 로그인 후 사용자 B의 정보 수정 시도 시 403 에러 확인
    - 관리자로 로그인 후 모든 사용자 정보 수정 가능 확인

3. **토큰 만료 테스트**:
    - 만료된 토큰으로 요청 시 401 에러 확인
    - 로그인으로 새 토큰 발급 후 동일 요청 성공 확인

4. **비밀번호 암호화 확인**:
    - 데이터베이스에서 비밀번호가 암호화되어 저장되었는지 확인
    - 잘못된 비밀번호로 로그인 시도 시 실패 확인