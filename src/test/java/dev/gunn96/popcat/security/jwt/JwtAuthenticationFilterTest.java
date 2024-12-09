package dev.gunn96.popcat.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gunn96.popcat.common.ApiResponse;
import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.service.GeoIpService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private GeoIpService geoIpService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                authenticationManager,
                jwtProvider,
                geoIpService,
                objectMapper
        );
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
                .ipAddress(ipAddress)
                .regionCode(regionCode)
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
    @DisplayName("토큰이 없는 경우 새 토큰 발급")
    void doFilterInternal_NoToken() throws ServletException, IOException {
        // given
        String ipAddress = "127.0.0.1";
        String regionCode = "KR";
        String newToken = "new.token";

        given(request.getHeader("Authorization")).willReturn(null);
        given(request.getHeader("X-Forwarded-For")).willReturn(ipAddress);
        given(geoIpService.findRegionCodeByIpAddress(ipAddress)).willReturn(regionCode);
        given(jwtProvider.generateToken(ipAddress, regionCode)).willReturn(newToken);
        given(response.getWriter()).willReturn(writer);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(any(PrintWriter.class), argThat(response ->
                response instanceof ApiResponse && ((ApiResponse<?>) response).getData() instanceof PopResponse
                        && ((PopResponse) ((ApiResponse<?>) response).getData()).isProcessed() == false
        ));
        verifyNoInteractions(filterChain);
    }


    @Test
    @DisplayName("유효하지 않은 토큰의 경우 401 에러 반환")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        // given
        String token = "invalid.token";
        String bearerToken = "Bearer " + token;
        String ipAddress = "127.0.0.1";

        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(request.getHeader("X-Forwarded-For")).willReturn(ipAddress);
        given(authenticationManager.authenticate(any(JwtAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Invalid token"));
        given(response.getWriter()).willReturn(writer);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(any(PrintWriter.class), argThat(response ->
                response instanceof ApiResponse && !((ApiResponse<?>) response).isSuccess()
        ));
        verifyNoInteractions(filterChain);
    }
}