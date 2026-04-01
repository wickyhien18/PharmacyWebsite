package com.example.Pharmacy.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(cs -> cs.disable())
                .authorizeHttpRequests(auth ->  auth
                        .requestMatchers(HttpMethod.GET,"/roles/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/medicines/**").permitAll()
                        .requestMatchers("/medicines/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/categories/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
