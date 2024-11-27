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
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰으로 인증 성공")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        String ipAddress = "127.0.0.1";
        String regionCode = "UNKNOWN";

        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(request.getRemoteAddr()).willReturn(ipAddress);
        given(jwtProvider.validateToken(token, ipAddress, regionCode))
                .willReturn(TokenClaims.builder()
                        .id("id")
                        .issuer("issuer")
                        .audience(ipAddress)
                        .subject(regionCode)
                        .issuedAt(0)
                        .notBefore(0)
                        .expiresAt(0)
                        .build());

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
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
}