package com.sparta.onboarding.assignment.jwt;

import com.sparta.onboarding.assignment.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // Token 식별자
    public static final String GRANT_TYPE = "Bearer";

    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public TokenDto generateToken(User user) {

        //accessToken 생성
        String accessToken = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRoleTypes())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        return TokenDto.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Cookie createCookie(String refreshToken) {
        String cookieName = "refreshToken";
        Cookie cookie = new Cookie(cookieName, refreshToken);
        // 쿠키 속성 설정
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        return cookie;
    }

    public String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(GRANT_TYPE+" "))
            return authorizationHeader.substring(GRANT_TYPE.length()+1);

        return null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 토큰이 만료되었을 때 true 반환
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | io.jsonwebtoken.security.SignatureException e) {

        } catch (ExpiredJwtException e) {

        } catch (UnsupportedJwtException e) {

        } catch (IllegalArgumentException e) {

        }
        return false;
    }


    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
