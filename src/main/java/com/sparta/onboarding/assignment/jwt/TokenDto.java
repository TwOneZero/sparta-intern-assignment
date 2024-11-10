package com.sparta.onboarding.assignment.jwt;

import lombok.Builder;

@Builder
public record TokenDto(
        String grantType,
        String accessToken,
        String refreshToken
) {
}
