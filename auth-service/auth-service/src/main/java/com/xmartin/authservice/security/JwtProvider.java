package com.xmartin.authservice.security;

import com.xmartin.authservice.controller.dto.RequestDto;
import com.xmartin.authservice.entity.AuthUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expirationms}")
    private Long expirationms;

    private final RouteValidator routeValidator;

    public String createToken(AuthUser authUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", authUser.getId());
        claims.put("role", authUser.getRole());

        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(authUser.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationms))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token, RequestDto requestDto) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
        } catch (Exception e) {
            return false;
        }

        if (!isAdmin(token) && routeValidator.isAdminPath(requestDto)) {
            return false;
        } else {
            return true;
        }

    }

    private boolean isAdmin(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role")
                .equals("ROLE_ADMIN");
    }

    public String getEmailFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return "bad token";
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
