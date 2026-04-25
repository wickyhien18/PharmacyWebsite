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

    @Value("${app.jwt.expiration}")
    private long expiration;

    // Tạo token từ username
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(
                        System.currentTimeMillis() + expiration))
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

    // Kiểm tra token còn hạn không
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);

            System.out.println("Token hợp lệ");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi khác: " + e.getMessage());
        }
        return false;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
