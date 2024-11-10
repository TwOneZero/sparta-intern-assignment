package com.sparta.onboarding.assignment.auth.request;

import com.sparta.onboarding.assignment.auth.dto.AuthDto;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "사용자 이름은 비어있을 수 없습니다.")
        String username,
        @NotBlank(message = "사용자 비밀번호는 비어있을 수 없습니다.")
        String password
) {

        public AuthDto toAuthDto() {
                return AuthDto.builder()
                        .username(username)
                        .password(password)
                        .build();
        }
}
