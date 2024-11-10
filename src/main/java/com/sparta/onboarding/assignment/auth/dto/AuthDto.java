package com.sparta.onboarding.assignment.auth.dto;

import lombok.Builder;

@Builder
public record AuthDto(
        String username,
        String nickname,
        String password
) {
}
