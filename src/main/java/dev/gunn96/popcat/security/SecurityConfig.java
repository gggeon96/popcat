package dev.gunn96.popcat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationFilter;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationProvider;
import dev.gunn96.popcat.security.jwt.JwtProvider;
import dev.gunn96.popcat.service.GeoIpService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            AuthenticationConfiguration authConfig, JwtProvider jwtProvider,
            GeoIpService geoIpService, ObjectMapper objectMapper) throws Exception {
        return new JwtAuthenticationFilter(authConfig.getAuthenticationManager(),
                jwtProvider, geoIpService, objectMapper
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationProvider jwtAuthenticationProvider,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/v1/leaderboard").permitAll()
                        .requestMatchers("/api/v1/pop/**").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(jwtAuthenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .securityMatcher("/api/v1/pop/**")
                .build();
    }
}