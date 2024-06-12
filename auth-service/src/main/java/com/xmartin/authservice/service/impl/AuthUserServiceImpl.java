package com.xmartin.authservice.service.impl;

import com.xmartin.authservice.controller.dto.LoginDto;
import com.xmartin.authservice.controller.dto.RegisterDto;
import com.xmartin.authservice.controller.dto.RequestDto;
import com.xmartin.authservice.controller.dto.TokenDto;
import com.xmartin.authservice.entity.AuthUser;
import com.xmartin.authservice.repository.AuthUserRepository;
import com.xmartin.authservice.security.JwtProvider;
import com.xmartin.authservice.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public AuthUser save(RegisterDto registerDto) {
        Optional<AuthUser> user = authUserRepository.findByEmailIgnoreCase(registerDto.getEmail());
        if (user.isPresent()) return null;

        String password = passwordEncoder.encode(registerDto.getPassword());

        AuthUser authUser = AuthUser.builder()
                .name(registerDto.getName())
                .email(registerDto.getEmail())
                .password(password)
                .role("ROLE_USER")
                .build();

        return authUserRepository.save(authUser);
    }

    @Override
    public TokenDto login(LoginDto loginDto) {
        Optional<AuthUser> user = authUserRepository.findByEmailIgnoreCase(loginDto.getEmail());
        if (!user.isPresent()) return null;
        if (passwordEncoder.matches(loginDto.getPassword(), user.get().getPassword())) {
            return new TokenDto(jwtProvider.createToken(user.get()));
        } else {
            return null;
        }
    }

    @Override
    public TokenDto validate(String token, RequestDto requestDto) {
        if (!jwtProvider.validate(token, requestDto)) return null;

        String email = jwtProvider.getEmailFromToken(token);
        if (authUserRepository.findByEmailIgnoreCase(email).isEmpty()) return null;

        return new TokenDto(token);
    }


}
