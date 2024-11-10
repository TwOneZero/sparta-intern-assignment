package com.sparta.onboarding.assignment.auth;


import com.sparta.onboarding.assignment.auth.request.LoginRequest;
import com.sparta.onboarding.assignment.auth.request.RegisterRequest;
import com.sparta.onboarding.assignment.auth.response.LoginResponse;
import com.sparta.onboarding.assignment.auth.response.RegisterResponse;
import com.sparta.onboarding.assignment.jwt.JwtUtil;
import com.sparta.onboarding.assignment.jwt.TokenDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(
                RegisterResponse.from(authService.register(request.toAuthDto()))
        );
    }

    @PostMapping("/sign")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ){
        TokenDto tokenDto = authService.login(request.toAuthDto());
        // 리프레쉬 토큰 쿠키
        Cookie refreshTokenCookie = jwtUtil.createCookie(tokenDto.refreshToken());
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok(
                new LoginResponse(tokenDto.accessToken())
        );
    }

    @GetMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
            ) {
        // 쿠키에서 refreshToken 가져오기
        String refreshToken = getRefreshTokenFromCookies(request);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(null);  // 유효하지 않거나 없으면 Unauthorized 반환
        }
        TokenDto tokenDto = authService.refresh(refreshToken);
        Cookie refreshTokenCookie = jwtUtil.createCookie(tokenDto.refreshToken());
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok(new LoginResponse(tokenDto.accessToken()));
    }

    // 쿠키에서 refreshToken을 찾는 유틸리티 메소드
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
