package dev.gunn96.popcat.security.jwt;

import lombok.Builder;

@Builder
public record TokenClaims(
        String id,
        String issuer,
        String audience,  // IP address
        String subject,   // region code
        long issuedAt,
        long notBefore,
        long expiresAt
) {}