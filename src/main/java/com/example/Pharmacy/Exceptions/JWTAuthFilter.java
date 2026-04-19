package com.example.Pharmacy.Exceptions;

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

import com.example.Pharmacy.Services.JWTService;
import com.example.Pharmacy.Services.CustomUserDetailService;

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

        // Lấy header Authorization
        String header = request.getHeader("Authorization");

        // Không có token → bỏ qua, tiếp tục
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Cắt bỏ "Bearer " lấy token
        String token = header.substring(7);

        // Token không hợp lệ → bỏ qua
        if (!jwtService.isValid(token)) {
            chain.doFilter(request, response);
            return;
        }

        // Lấy username từ token
        String username = jwtService.getUsername(token);

        // Set authentication vào SecurityContext
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}
