package dev.gunn96.popcat.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰으로 인증 성공")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        String ipAddress = "127.0.0.1";
        String regionCode = "KR";

        TokenClaims claims = TokenClaims.builder()
                .id("id")
                .issuer("issuer")
                .audience(ipAddress)
                .subject(regionCode)
                .issuedAt(0)
                .notBefore(0)
                .expiresAt(0)
                .build();

        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(request.getHeader("X-Forwarded-For")).willReturn(ipAddress);
        given(authenticationManager.authenticate(any(JwtAuthenticationToken.class)))
                .willReturn(new JwtAuthenticationToken(claims, ipAddress, Collections.emptyList()));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        verify(filterChain).doFilter(request, response);
        verify(authenticationManager).authenticate(any(JwtAuthenticationToken.class));
    }

    @Test
    @DisplayName("Authorization 헤더가 없는 경우 인증 건너뛰기")
    void doFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
        // given
        given(request.getHeader("Authorization")).willReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 토큰이 아닌 경우 인증 건너뛰기")
    void doFilterInternal_NotBearerToken() throws ServletException, IOException {
        // given
        given(request.getHeader("Authorization")).willReturn("Basic abc123");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("인증 실패시 SecurityContext가 초기화되는지 확인")
    void doFilterInternal_AuthenticationFails_ClearsSecurityContext() throws ServletException, IOException {
        // given
        String token = "invalid.jwt.token";
        String bearerToken = "Bearer " + token;
        String ipAddress = "127.0.0.1";

        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(request.getHeader("X-Forwarded-For")).willReturn(ipAddress);
        given(authenticationManager.authenticate(any(JwtAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Invalid token"));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}