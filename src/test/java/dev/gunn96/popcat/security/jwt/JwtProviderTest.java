package dev.gunn96.popcat.security.jwt;

import dev.gunn96.popcat.exception.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private static final String SECRET = "thisIsTestSecretKeyForJwtProviderTestthisIsTestSecretKeyForJwtProviderTest";
    private static final String SERVER_ADDRESS = "127.0.0.1:50001";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String REGION_CODE = "KR";
    private static final long EXPIRATION_SECONDS = 1;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, SERVER_ADDRESS, EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void generateToken_Success() {
        // when
        String token = jwtProvider.generateToken(IP_ADDRESS, REGION_CODE);

        // then
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("토큰 검증 성공")
    void validateToken_Success() {
        // given
        String token = jwtProvider.generateToken(IP_ADDRESS, REGION_CODE);

        // when
        TokenClaims claims = jwtProvider.validateToken(token, IP_ADDRESS, REGION_CODE);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.audience()).isEqualTo(IP_ADDRESS);
        assertThat(claims.subject()).isEqualTo(REGION_CODE);
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_EmptyToken() {
        assertThatThrownBy(() -> jwtProvider.validateToken("", IP_ADDRESS, REGION_CODE))
                .isInstanceOf(JwtException.EmptyTokenException.class);
    }

    @Test
    @DisplayName("잘못된 IP로 토큰 검증 실패")
    void validateToken_WrongIpAddress() {
        // given
        String token = jwtProvider.generateToken(IP_ADDRESS, REGION_CODE);
        String wrongIp = "192.168.0.1";

        // when, then
        assertThatThrownBy(() -> jwtProvider.validateToken(token, wrongIp, REGION_CODE))
                .isInstanceOf(JwtException.InvalidTokenException.class);
    }

    @Test
    @DisplayName("잘못된 Region으로 토큰 검증 실패")
    void validateToken_WrongRegion() {
        // given
        String token = jwtProvider.generateToken(IP_ADDRESS, REGION_CODE);
        String wrongRegion = "US";

        // when, then
        assertThatThrownBy(() -> jwtProvider.validateToken(token, IP_ADDRESS, wrongRegion))
                .isInstanceOf(JwtException.InvalidTokenException.class);
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_ExpiredToken() throws InterruptedException {
        // given
        String token = jwtProvider.generateToken(IP_ADDRESS, REGION_CODE);

        // 토큰 만료 대기 (1.1초)
        Thread.sleep(1100);

        // when, then
        assertThatThrownBy(() -> jwtProvider.validateToken(token, IP_ADDRESS, REGION_CODE))
                .isInstanceOf(JwtException.InvalidTokenException.class);
    }
}