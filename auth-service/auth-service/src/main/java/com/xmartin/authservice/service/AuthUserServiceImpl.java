package com.xmartin.authservice.service;

import com.xmartin.authservice.controller.dto.AuthUserDto;
import com.xmartin.authservice.controller.dto.TokenDto;
import com.xmartin.authservice.entity.AuthUser;
import com.xmartin.authservice.repository.AuthUserRepository;
import com.xmartin.authservice.security.JwtProvider;
import com.xmartin.authservice.service.impl.AuthUserService;
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
    public AuthUser save(AuthUserDto authUserDto) {
        Optional<AuthUser> user = authUserRepository.findByEmailIgnoreCase(authUserDto.getEmail());
        if (user.isPresent()) return null;

        String password = passwordEncoder.encode(authUserDto.getPassword());

        AuthUser authUser = AuthUser.builder()
                .email(authUserDto.getEmail())
                .password(password)
                .build();

        return authUserRepository.save(authUser);
    }

    @Override
    public TokenDto login(AuthUserDto authUserDto) {
        Optional<AuthUser> user = authUserRepository.findByEmailIgnoreCase(authUserDto.getEmail());
        if (!user.isPresent()) return null;
        if (passwordEncoder.matches(authUserDto.getPassword(), user.get().getPassword())) {
            return new TokenDto(jwtProvider.createToken(user.get()));
        } else {
            return null;
        }
    }

    @Override
    public TokenDto validate(String token) {
        if (!jwtProvider.validate(token)) return null;

        String email = jwtProvider.getEmailFromToken(token);
        if (authUserRepository.findByEmailIgnoreCase(email).isEmpty()) return null;

        return new TokenDto(token);
    }


}
