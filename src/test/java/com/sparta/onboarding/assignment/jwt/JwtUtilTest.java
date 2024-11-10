package com.sparta.onboarding.assignment.jwt;

import com.sparta.onboarding.assignment.user.RoleType;
import com.sparta.onboarding.assignment.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User user;
    private String secretKey = "adklfjakldfjklj1pjklajfdkljakldfjaklj2103jkfdljalkfdadf";
    private long accessTokenExpiration = 3600000;
    private long refreshTokenExpiration = 86400000;


    @BeforeAll
    public void setUp() {
        jwtUtil = new JwtUtil();
        //jwtUtil 의 Value 대신 값 삽입
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", refreshTokenExpiration);
        user = User.builder()
                .id(1L).username("testUsername").nickname("testNickname").password("testPassword")
                .roleTypes(Set.of(RoleType.USER)).build();
    }

    @DisplayName("토큰 생성 테스트")
    @Test
    public void generateTokenTest(){
        TokenDto tokenDto = jwtUtil.generateToken(user);

        System.out.println("AccessToken : " + tokenDto.accessToken());
        System.out.println("RefreshToken: " + tokenDto.refreshToken());

        assertNotNull(tokenDto.accessToken());
        assertNotNull(tokenDto.refreshToken());
        assertEquals(JwtUtil.GRANT_TYPE, tokenDto.grantType());

        // 토큰의 유효성 검사
        assertTrue(jwtUtil.validateToken(tokenDto.accessToken()));
        assertTrue(jwtUtil.validateToken(tokenDto.refreshToken()));
    }

    @DisplayName("잘못된 토큰 유효성 테스트")
    @Test
    public void testValidateToken_withInvalidToken() {
        String invalidToken = "invalid.token.value";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid, "Token should be invalid");
    }

    @DisplayName("만료 시간 지난 토큰 검증 -> True 반환")
    @Test
    public void testIsTokenExpired_withExpiredToken() {
        // 만료 시간이 매우 짧은 토큰을 생성하여 만료 상태를 테스트
        Date now = new Date();
        String expiredToken = Jwts.builder()
                .setSubject("testUsername")
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 만료된 토큰
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        assertTrue(isExpired, "Token should be expired");
    }

    @DisplayName("토큰에서 유저 이름 추출 테스트")
    @Test
    public void testExtractUsername() {
        String token = jwtUtil.generateToken(user).accessToken();

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals("testUsername", extractedUsername, "Extracted username should match user ID");
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}