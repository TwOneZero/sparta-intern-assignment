package com.sparta.onboarding.assignment.security;

import com.sparta.onboarding.assignment.user.RoleType;
import com.sparta.onboarding.assignment.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Set;

@RequiredArgsConstructor
public class CustomMockSecurityContext implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {

        var principal = User.builder()
                .username(annotation.username()).nickname(annotation.nickname()).password(annotation.password())
                .roleTypes(Set.of(RoleType.USER))
                .build();
        var authToken = new UsernamePasswordAuthenticationToken(principal, annotation.password(), principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        return context;
    }
}