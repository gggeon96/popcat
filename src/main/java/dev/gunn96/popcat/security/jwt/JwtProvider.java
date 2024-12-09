package dev.gunn96.popcat.security.jwt;


import dev.gunn96.popcat.exception.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {
    private final SecretKey key;
    private final String serverIdentifier;
    private final long expirationSeconds;
    private final JwtParser jwtParser;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.server-address}") String serverAddress,
            @Value("${jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.serverIdentifier = generateServerIdentifier(serverAddress, secret);
        this.expirationSeconds = expirationSeconds;
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }

    public String generateToken(String ipAddress, String regionCode) {
        Instant now = Instant.now();

        TokenClaims claims = TokenClaims.builder()
                .id(UUID.randomUUID().toString())
                .issuer(serverIdentifier)
                .ipAddress(ipAddress)
                .regionCode(regionCode)
                .issuedAt(now.getEpochSecond())
                .notBefore(now.minusSeconds(600).getEpochSecond())
                .expiresAt(now.plusSeconds(expirationSeconds).getEpochSecond())
                .build();

        return Jwts.builder()
                .id(claims.id())
                .issuer(claims.issuer())
                .audience().add(claims.ipAddress()).and()
                .subject(claims.regionCode())
                .issuedAt(Date.from(Instant.ofEpochSecond(claims.issuedAt())))
                .notBefore(Date.from(Instant.ofEpochSecond(claims.notBefore())))
                .expiration(Date.from(Instant.ofEpochSecond(claims.expiresAt())))
                .signWith(key)
                .compact();
    }

    public TokenClaims validateToken(String token, String ipAddress) {
        try {
            Claims claims = jwtParser
                    .parseSignedClaims(token)
                    .getPayload();

            // Validate issuer and audience
            if (!claims.getIssuer().equals(serverIdentifier) ||
                    !claims.getAudience().contains(ipAddress)) {
                throw new JwtException.InvalidTokenException("Invalid token claims");
            }

            return TokenClaims.builder()
                    .id(claims.getId())
                    .issuer(claims.getIssuer())
                    .ipAddress(claims.getAudience().iterator().next())
                    .regionCode(claims.getSubject())
                    .issuedAt(claims.getIssuedAt().toInstant().getEpochSecond())
                    .notBefore(claims.getNotBefore().toInstant().getEpochSecond())
                    .expiresAt(claims.getExpiration().toInstant().getEpochSecond())
                    .build();

        } catch (ExpiredJwtException e) {
            throw e;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException |
                 IllegalArgumentException e) {
            throw new JwtException.InvalidTokenException("Invalid JWT token", e);
        }
    }

    private String generateServerIdentifier(String serverAddress, String secret) {
        return UUID.nameUUIDFromBytes(
                (serverAddress + secret).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}