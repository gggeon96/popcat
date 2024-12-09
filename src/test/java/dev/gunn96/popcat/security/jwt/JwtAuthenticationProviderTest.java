package dev.gunn96.popcat.security.jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    @Mock
    private JwtProvider jwtProvider;

    private JwtAuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        authenticationProvider = new JwtAuthenticationProvider(jwtProvider);
    }

    @Test
    @DisplayName("유효한 JWT 토큰 인증 성공")
    void authenticate_ValidToken_Success() {
        // given
        String token = "valid.jwt.token";
        String ipAddress = "127.0.0.1";
        String regionCode = "KR";
        TokenClaims expectedClaims = TokenClaims.builder()
                .id("id")
                .issuer("issuer")
                .ipAddress(ipAddress)
                .regionCode(regionCode)
                .issuedAt(0)
                .notBefore(0)
                .expiresAt(0)
                .build();

        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token, ipAddress);
        given(jwtProvider.validateToken(token, ipAddress)).willReturn(expectedClaims);

        // when
        Authentication result = authenticationProvider.authenticate(authRequest);

        // then
        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(((JwtAuthenticationToken) result).getIpAddress()).isEqualTo(ipAddress);
        assertThat(result.getPrincipal()).isEqualTo(expectedClaims);
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 인증 실패")
    void authenticate_InvalidToken_ThrowsException() {
        // given
        String token = "invalid.jwt.token";
        String ipAddress = "127.0.0.1";
        String regionCode = "KR";
        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token, ipAddress);

        given(jwtProvider.validateToken(token, ipAddress))
                .willThrow(new JwtException("Invalid token")); // 여기를 수정

        // when & then
        assertThatThrownBy(() -> authenticationProvider.authenticate(authRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasCauseInstanceOf(io.jsonwebtoken.JwtException.class);  // 여기도 수정
    }

    @Test
    @DisplayName("지원하지 않는 Authentication 타입 검증")
    void supports_UnsupportedType_ReturnsFalse() {
        // when & then
        assertThat(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class)).isFalse();
    }

    @Test
    @DisplayName("지원하는 Authentication 타입 검증")
    void supports_SupportedType_ReturnsTrue() {
        // when & then
        assertThat(authenticationProvider.supports(JwtAuthenticationToken.class)).isTrue();
    }
}