package com.example.Pharmacy.Services;

import com.example.Pharmacy.Repositories.UserRepository;
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
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final CustomUserDetailService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Bỏ qua filter cho API auth
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

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
        if (!jwtService.validateAccessToken(token)) {
            System.out.println("Token không phải access token hoặc không hợp lệ");
            chain.doFilter(request, response);
            return;
        }

        // Lấy username từ token
        String username = jwtService.getUsername(token);

        // Set authentication vào SecurityContext
        if (username != null) {
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                userRepository.updateLastActivity(username, LocalDateTime.now());
            }
        }

        chain.doFilter(request, response);
    }
}
