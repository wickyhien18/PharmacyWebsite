package com.example.Pharmacy.Services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final CustomUserDetailService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        // Log URI để debug
        System.out.println("=== JWTAuthFilter ===");
        System.out.println("Request URI: " + request.getRequestURI());

        // Lấy header Authorization
        String header = request.getHeader("Authorization");
        System.out.println("Authorization header: " + (header != null ? header.substring(0, Math.min(header.length(), 50)) : "null"));

        // Kiểm tra header
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("Không có token hoặc sai format, bỏ qua filter");
            chain.doFilter(request, response);
            return;
        }

        // Cắt bỏ "Bearer " lấy token
        String token = header.substring(7);
        System.out.println("Token: " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));

        // Kiểm tra token hợp lệ
        boolean isValid = jwtService.isValid(token);
        System.out.println("Token hợp lệ: " + isValid);

        if (!isValid) {
            System.out.println("Token không hợp lệ, bỏ qua filter");
            chain.doFilter(request, response);
            return;
        }

        // Lấy username từ token
        String username = jwtService.getUsername(token);
        System.out.println("Username từ token: " + username);

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        System.out.println("User roles: " + userDetails.getAuthorities());

        // Set authentication
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        System.out.println("Đã set authentication thành công cho: " + username);

        chain.doFilter(request, response);
    }
}
