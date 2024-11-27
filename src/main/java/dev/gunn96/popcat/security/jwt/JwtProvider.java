package dev.gunn96.popcat.security.jwt;


import dev.gunn96.popcat.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.server-address}") String serverAddress,
            @Value("${jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.serverIdentifier = generateServerIdentifier(serverAddress, secret);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String ipAddress, String regionCode) {
        Instant now = Instant.now();

        TokenClaims claims = TokenClaims.builder()
                .id(UUID.randomUUID().toString())
                .issuer(serverIdentifier)
                .audience(ipAddress)
                .subject(regionCode)
                .issuedAt(now.getEpochSecond())
                .notBefore(now.minusSeconds(60).getEpochSecond())
                .expiresAt(now.plusSeconds(expirationSeconds).getEpochSecond())
                .build();

        return Jwts.builder()
                .id(claims.id())
                .issuer(claims.issuer())
                .audience().add(claims.audience()).and()
                .subject(claims.subject())
                .issuedAt(Date.from(Instant.ofEpochSecond(claims.issuedAt())))
                .notBefore(Date.from(Instant.ofEpochSecond(claims.notBefore())))
                .expiration(Date.from(Instant.ofEpochSecond(claims.expiresAt())))
                .signWith(key)
                .compact();
    }

    public TokenClaims validateToken(String token, String ipAddress, String regionCode) {
        if (token == null || token.isBlank()) {
            throw new JwtException.EmptyTokenException();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Validate issuer, audience and subject
            if (!claims.getIssuer().equals(serverIdentifier) ||
                    !claims.getAudience().contains(ipAddress) ||
                    !claims.getSubject().equals(regionCode)) {
                throw new JwtException.InvalidTokenException("Invalid token claims");
            }

            return TokenClaims.builder()
                    .id(claims.getId())
                    .issuer(claims.getIssuer())
                    .audience(claims.getAudience().iterator().next())
                    .subject(claims.getSubject())
                    .issuedAt(claims.getIssuedAt().toInstant().getEpochSecond())
                    .notBefore(claims.getNotBefore().toInstant().getEpochSecond())
                    .expiresAt(claims.getExpiration().toInstant().getEpochSecond())
                    .build();

        } catch (ExpiredJwtException e) {
            throw new JwtException.InvalidTokenException("Token has expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException.InvalidTokenException("JWT validation failed", e);
        }
    }

    private String generateServerIdentifier(String serverAddress, String secret) {
        return UUID.nameUUIDFromBytes(
                (serverAddress + secret).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}