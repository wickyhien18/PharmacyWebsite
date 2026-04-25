package com.example.Pharmacy.Services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.sql.Date;

@Service
public class JWTService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration.access}")
    private Long accessExpiration;

    @Value("${app.jwt.expiration.refresh}")
    private Long refreshExpiration;

    // Tạo access token từ username
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(
                        System.currentTimeMillis() + accessExpiration))
                .claim("type","access")
                .signWith(getKey())
                .compact();
    }

    //Tạo refresh token từ username
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(
                        System.currentTimeMillis() + refreshExpiration))
                .claim("type","refresh")
                .signWith(getKey())
                .compact();
    }

    // Lấy username từ token
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            System.out.println("Giải mã token thành công, username: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("Giải mã token thất bại: " + e.getMessage());
            return null;
        }
    }

    // Kiểm tra access token hợp lệ
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return "access".equals(type) && !claims.getExpiration().before(new Date(System.currentTimeMillis()));
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi khác: " + e.getMessage());
        }
        return false;
    }

    // Kiểm tra refresh token hợp lệ
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return "refresh".equals(type) && !claims.getExpiration().before(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            return false;
        }
    }

    // Lấy loại token (access/refresh)
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
