package Pharmacy.Config;


import Pharmacy.Entities.Users;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JWTUtil {
    //Secret key
    @Value("${app.jwt.secret}")
    private String secret;
    //Secret Key for register Token
    //The Length  > 32 characters
    //Use for HMAC-SH256 aka Hash-based Message Authentication Code - Hashing with result 256 bits

    //Expiration of Access Token
    @Value("${app.jwt.expiration.access}")
    private Long accessExpiration;
    //3600000 mls - 1h

    public String generateAccessToken(Users users) {
        return Jwts.builder()
                .subject(users.getEmail())
                //Register Token with Email
                .claim("userId", users.getUserId())
                .claim("role", users.getRoles() != null ? users.getRoles().getRoleName() : "")
                //Add role into Token -> frontend know Role
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + accessExpiration))
                .signWith(getSecretKey())
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String generateRefreshToken() {

        //Create Random 256-bit String
        //SecureRandom() > Math.random() and more security
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
        // Result String ~43 characters, example: "xK9mP2qR8vL4nJ6wT0yU3oI5hF7cB1eD"
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
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Get HashKey
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
