package com.xmartin.userservice.interceptors;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        template.header("Authorization", "Bearer " + jwt.getTokenValue());
    }
}