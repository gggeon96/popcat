package dev.gunn96.popcat.security.jwt;

import lombok.Builder;

@Builder
public record TokenClaims(
        String id,
        String issuer,
        String ipAddress,
        String regionCode,
        long issuedAt,
        long notBefore,
        long expiresAt
) {}