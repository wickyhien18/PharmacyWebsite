package Pharmacy.Services;


import Pharmacy.Entities.Users;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class JWTService {
    //Secret key
    @Value("${app.jwt.secret}")
    private String secret;

    //Expiration of Access Token
    @Value("${app.jwt.expiration.access}")
    private Long accessExpiration;

    public String generateAccessToken(Users users) {
        return Jwts.builder()
                .subject(users.getEmail())
                .claim("userId", users.getUserId())
                .claim("role", users.getRoles() != null ? users.getRoles().getRoleName() : "")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + accessExpiration))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String generateRefreshToken() {
        // SecureRandom đảm bảo không đoán được (khác với Random thông thường)
        byte[] randomBytes = new byte[32];       // 256 bits
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()           // Bỏ dấu = cuối cho gọn
                .encodeToString(randomBytes);
        // Kết quả: chuỗi ~43 ký tự, ví dụ: "xK9mP2qR8vL4nJ6wT0yU3oI5hF7cB1eD"
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Access token hết hạn");
        } catch (JwtException e) {
            System.out.println("Access token không hợp lệ: " + e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Get HashKey
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
