# 내일배움캠프 한달인턴 온보딩 과제 (JAVA)

- **API docs** : [Swagger](http://43.200.21.99:8080/swagger-ui/index.html#/)

## Features
- Spring Security + JWT 를 활용한 인증, 인가 기능
  -  Token 리프레시 기능
- Jwt 관련 단위 테스트 작성
- 로그인, 회원가입 컨트롤러 API 단위 테스트 작성
- EC2 배포

## User
- UserDetails 를 구현한 User 엔티티 작성
- User 의 role 을 여러 개 저장하도록 Set 으로 필드를 정의
- RoleTypesConverter 를 통해 DB 저장 <-> 객체 변환을 편리하게 구성
```java
@Converter()
public class RoleTypesConverter implements AttributeConverter<Set<RoleType>, String> {
    private static final String DELIMITER = ",";
    @Override
    public String convertToDatabaseColumn(Set<RoleType> attribute) {
        return attribute.stream().map(RoleType::name).sorted().collect(Collectors.joining(DELIMITER));
    }
    @Override
    public Set<RoleType> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(DELIMITER)).map(RoleType::valueOf).collect(Collectors.toSet());
    }
}
```
### 회원가입 ( POST /signup )
- Request Message
```json
{
  "username": "JIN HO",
  "password": "12341234",
  "nickname": "Mentos"
}
```

- Response Message
```json
{
  "username": "JIN HO",
  "nickname": "Mentos",
  "authorities": [
    {
      "authorityName": "ROLE_USER"
    }
  ]		
}
```
### 로그인 ( POST /sign )
- Request Message
```json
{
	"username": "JIN HO",
	"password": "12341234"
}
```

- Response Message
```json
{
	"token": "eKDIkdfjoakIdkfjpekdkcjdkoIOdjOKJDFOlLDKFJKL",
}
```
### 리프레쉬 토큰 ( GET /refresh )
- Cookie 의 정보를 통해 refresh
- Response Message
```json
{
	"token": "eKDIkdfjoakIdkfjpekdkcjdkoIOdjOKJDFOlLDKFJKL",
}
```

### 개선 필요 보안점
- 리프레쉬 토큰 블랙리스트
  - 토큰 로테이션으로 accessToken, refreshToken 을 모두 재발급받아도, 생명주기가 긴 refreshToken 은 위험이 있기에 Redis 등에 저장하여 조회해서 검증 후 사용하도록 할 필요가 있음

- 요청 IP 검증을 통해 알림 보내기

