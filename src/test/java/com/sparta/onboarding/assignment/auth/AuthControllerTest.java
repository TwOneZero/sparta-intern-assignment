package com.sparta.onboarding.assignment.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.onboarding.assignment.auth.request.LoginRequest;
import com.sparta.onboarding.assignment.auth.request.RegisterRequest;
import com.sparta.onboarding.assignment.auth.response.RegisterResponse;
import com.sparta.onboarding.assignment.jwt.JwtUtil;
import com.sparta.onboarding.assignment.jwt.TokenDto;
import com.sparta.onboarding.assignment.security.TestSecurityConfig;
import com.sparta.onboarding.assignment.user.RoleType;
import com.sparta.onboarding.assignment.user.dto.UserDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import({TestSecurityConfig.class})
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @MockBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @DisplayName("회원가입 성공")
    @Test
    void testRegister() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("testUsername", "testNickname", "testPassword");
        UserDto user = createUserDto();
        RegisterResponse response = RegisterResponse.from(user);

        when(authService.register(any())).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(response.username()))
                .andExpect(jsonPath("$.nickname").value(response.nickname()))
                .andExpect(jsonPath("$.authorities").isArray())
                .andExpect(jsonPath("$.authorities[0].authorityName").value("ROLE_USER"));
    }

    @DisplayName("로그인 성공")
    @Test
    void testLogin() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testUser", "testPassword");
        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";

        TokenDto tokenDto = TokenDto.builder()
                .grantType("Bearer")
                .accessToken(mockAccessToken)
                .refreshToken(mockRefreshToken)
                .build();

        when(authService.login(any())).thenReturn(tokenDto);
        when(jwtUtil.createCookie(any())).thenReturn(new Cookie("refreshToken",mockRefreshToken));

        // When & Then
        mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockAccessToken"));
    }

    private UserDto createUserDto(){
        return UserDto.builder()
                .id(1L)
                .username("testUsername")
                .nickname("testNickname")
                .password("testPassword")
                .roleTypes(Set.of(RoleType.USER))
                .build();
    }

    @DisplayName("리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급")
    @Test
    void testRefreshToken() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        TokenDto tokenDto = TokenDto.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

        // Mocking
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(authService.refresh(refreshToken)).thenReturn(tokenDto);
        when(jwtUtil.createCookie(any())).thenReturn(refreshCookie);


        // When & Then
        mockMvc.perform(get("/refresh")
                        .requestAttr("request", request)
                        .cookie(refreshCookie)) // 쿠키 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(newAccessToken));

        verify(jwtUtil).validateToken(refreshToken); // validateToken 호출 검증
        verify(authService).refresh(refreshToken); // refresh 호출 검증
    }
}