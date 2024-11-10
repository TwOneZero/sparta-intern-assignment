package com.sparta.onboarding.assignment.auth.response;

import com.sparta.onboarding.assignment.user.dto.UserDto;
import lombok.Builder;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record RegisterResponse(
        String username,
        String nickname,
        Set<AuthorityResponse> authorities
) {
    public record AuthorityResponse(String authorityName){}

    public static RegisterResponse from(UserDto dto) {
        return RegisterResponse.builder()
                .username(dto.username())
                .nickname(dto.nickname())
                .authorities(dto.roleTypes().stream()
                        .map(auth -> new AuthorityResponse(auth.getRoleName()))
                        .collect(Collectors.toUnmodifiableSet())
                )
                .build();
    }
}