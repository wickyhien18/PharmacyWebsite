package Pharmacy.Services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.Base64;

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
    public String generateRefreshToken() {
        // SecureRandom đảm bảo unpredictable
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Hash trước khi lưu DB — SHA-256 là đủ cho refresh token
    public String hashRefreshToken(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có trong JVM — exception này không bao giờ xảy ra
            throw new IllegalStateException("SHA-256 not available", e);
        }
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

            System.out.println("Token type: " + claims.get("type", String.class));
            System.out.println("Token expiry: " + claims.getExpiration());

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
