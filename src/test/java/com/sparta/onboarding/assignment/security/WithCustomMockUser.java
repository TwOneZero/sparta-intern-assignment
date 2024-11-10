package com.sparta.onboarding.assignment.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomMockSecurityContext.class)
public @interface WithCustomMockUser {

    String nickname() default "testnickname";
    String username() default "testusername";
    String password() default "testpassword";
}
