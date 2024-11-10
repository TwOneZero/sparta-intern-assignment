package com.sparta.onboarding.assignment.user.dto;

import com.sparta.onboarding.assignment.user.RoleType;
import com.sparta.onboarding.assignment.user.User;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserDto(
        Long id,
        String username,
        String nickname,
        String password,
        Set<RoleType> roleTypes
) {

    public static UserDto from(User entity){
        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .nickname(entity.getNickname())
                .password(entity.getPassword())
                .roleTypes(entity.getRoleTypes())
                .build();
    }
}
