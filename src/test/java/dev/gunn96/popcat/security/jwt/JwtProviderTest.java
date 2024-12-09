package dev.gunn96.popcat.security.jwt;

import dev.gunn96.popcat.exception.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private SecretKey key;
    private String serverIdentifier;

    private static final String SECRET = "thisIsTestSecretKeyForJwtProviderTestthisIsTestSecretKeyForJwtProviderTest";
    private static final String SERVER_ADDRESS = "127.0.0.1:50001";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String REGION_CODE = "KR";
    private static final long EXPIRATION_SECONDS = 1;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, SERVER_ADDRESS, EXPIRATION_SECONDS);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        serverIdentifier = UUID.nameUUIDFromBytes(
                (SERVER_ADDRESS + SECRET).getBytes(StandardCharsets.UTF_8)
        ).toString();
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
        TokenClaims claims = jwtProvider.validateToken(token, IP_ADDRESS);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.ipAddress()).isEqualTo(IP_ADDRESS);
        assertThat(claims.regionCode()).isEqualTo(REGION_CODE);
    }

    @Test
    @DisplayName("잘못된 IP로 토큰 검증 실패")
    void validateToken_WrongIpAddress() {
        // given
        String token = jwtProvider.generateToken(IP_ADDRESS, REGION_CODE);
        String wrongIp = "192.168.0.1";

        // when, then
        assertThatThrownBy(() -> jwtProvider.validateToken(token, wrongIp))
                .isInstanceOf(JwtException.InvalidTokenException.class);
    }


    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_ExpiredToken_ThrowsException() {
        // given
        Instant now = Instant.now();
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(serverIdentifier)
                .audience().add(IP_ADDRESS).and()
                .subject(REGION_CODE)
                .issuedAt(Date.from(now.minusSeconds(10)))
                .notBefore(Date.from(now.minusSeconds(10)))
                .expiration(Date.from(now.minusSeconds(5)))  // 5초 전에 만료된 토큰
                .signWith(key)
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(token, IP_ADDRESS))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("expired");
    }
}