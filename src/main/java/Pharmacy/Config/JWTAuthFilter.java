package Pharmacy.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {
    // 1 Filter / 1 Request

    private final JWTUtil jwtService;
    private final CustomUserDetailService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip filter for AUTH API
        if (path.startsWith("/api/auth/") || (path.startsWith("/api/health"))) {
            chain.doFilter(request, response);
            return;
        }


        String header = request.getHeader("Authorization");

        // no token → skip, next
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;

            //SecurityConfig decide from here
            //Auth Endpoint  -> blocked by SecurityConfig, return 401
        }

        // Get Token skip header Bearer
        String token = header.substring(7);

        // Invalid Token → skip
        if (!jwtService.isValid(token)) {
            System.out.println("Token error: " + token);
            chain.doFilter(request, response);
            return;

            //SecurityContext don't have token -> Auth Endpoint -> 401
        }

        String email = jwtService.extractEmail(token);

        // Set authentication in SecurityContext
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            var auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
