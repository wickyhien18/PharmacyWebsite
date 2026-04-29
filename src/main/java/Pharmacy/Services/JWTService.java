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
    //Secret key
    @Value("${app.jwt.secret}")
    private String secret;

    //Expiration of Access Token
    @Value("${app.jwt.expiration.access}")
    private Long accessExpiration;

    //Expiration of Refresh Token
    @Value("${app.jwt.expiration.refresh}")
    private Long refreshExpiration;

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


    public String generateRefreshToken() {
        // SecureRandom guarantee unpredictable
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Hash before adding to database — SHA-256 is enough for refresh token
    public String hashRefreshToken(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // Get username from token
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

    // Check validate access token
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return "access".equals(type) && claims.getExpiration().after(new Date(System.currentTimeMillis()));
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi khác: " + e.getMessage());
        }
        return false;
    }

    //Get HashKey
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
