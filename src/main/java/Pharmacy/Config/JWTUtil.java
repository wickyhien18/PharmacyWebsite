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

// Indicates that an annotated class is a component and will be auto-detected.
@Component
/**
 * Class JWTUtil.
 * Provides functionality and data modeling for JWTUtil.
 */
public class JWTUtil {
    //Secret key
    // Indicates a default value expression for the annotated field.
    @Value("${app.jwt.secret}")
    private String secret;
    //Secret Key for register Token
    //The Length  > 32 characters
    //Use for HMAC-SH256 aka Hash-based Message Authentication Code - Hashing with result 256 bits

    //Expiration of Access Token
    // Indicates a default value expression for the annotated field.
    @Value("${app.jwt.expiration.access}")
    private Long accessExpiration;
    //3600000 mls - 1h

    /**
     * Generate access token.
     *
     * @param users the users
     * @return the String result
     */
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

    /**
     * Extract email.
     *
     * @param token the token
     * @return the String result
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Generate refresh token.
     *
     * @return the String result
     */
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

    /**
     * Checks if valid.
     *
     * @param token the token
     * @return the boolean result
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Access token expires");
        } catch (JwtException e) {
            System.out.println("Invalid access token:" + e.getMessage());
        }
        return false;
    }

    /**
     * Parse claims.
     *
     * @param token the token
     * @return the Claims result
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Get HashKey
    /**
     * Retrieves secret key.
     *
     * @return the SecretKey result
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
