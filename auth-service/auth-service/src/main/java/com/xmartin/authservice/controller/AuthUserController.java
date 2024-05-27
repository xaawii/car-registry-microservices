package com.xmartin.authservice.controller;

import com.xmartin.authservice.controller.dto.LoginDto;
import com.xmartin.authservice.controller.dto.RegisterDto;
import com.xmartin.authservice.controller.dto.RequestDto;
import com.xmartin.authservice.controller.dto.TokenDto;
import com.xmartin.authservice.entity.AuthUser;
import com.xmartin.authservice.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginDto loginDto) {
        TokenDto tokenDto = authUserService.login(loginDto);
        if (tokenDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(tokenDto);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenDto> validate(@RequestParam String token, @RequestBody RequestDto requestDto) {
        TokenDto tokenDto = authUserService.validate(token, requestDto);
        if (tokenDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(tokenDto);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<AuthUser> save(@RequestBody RegisterDto registerDto) {
        AuthUser authUser = authUserService.save(registerDto);
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(authUser);
        }
    }
}
