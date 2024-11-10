package com.sparta.onboarding.assignment.auth;


import com.sparta.onboarding.assignment.auth.dto.AuthDto;
import com.sparta.onboarding.assignment.jwt.JwtUtil;
import com.sparta.onboarding.assignment.jwt.TokenDto;
import com.sparta.onboarding.assignment.user.RoleType;
import com.sparta.onboarding.assignment.user.User;
import com.sparta.onboarding.assignment.user.UserRepository;
import com.sparta.onboarding.assignment.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserDto register(AuthDto authDto) {
        //중복 체크
        checkDuplicatedUser(authDto.username(), authDto.nickname());
        //회원가입
        User user = User.createUser(
                authDto.username(), passwordEncoder.encode(authDto.password()), authDto.nickname()
        );
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public TokenDto login(AuthDto authDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authDto.username(),
                        authDto.password()
                )
        );
        User user = userRepository.findByUsername(authDto.username()).orElseThrow(
                () -> new IllegalArgumentException("User not found with username")
        );
        return jwtUtil.generateToken(user);
    }

    private void checkDuplicatedUser(String username, String nickname) {
        if (userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("Duplicated username");
        }
        if (userRepository.existsByNickname(nickname)){
            throw new IllegalArgumentException("Duplicated nickname");
        }
    }

    public TokenDto refresh(String refreshToken) {
        // refreshToken을 통해 새로운 accessToken 발급
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("User not found with username")
        );
        TokenDto newTokenSet = jwtUtil.generateToken(user);

        return newTokenSet;
    }
}
