package dev.gunn96.popcat.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final String ipAddress;  // IP 주소 정보 추가
    private TokenClaims claims;

    // 인증 전
    public JwtAuthenticationToken(String token, String ipAddress) {
        super(null);
        this.token = token;
        this.ipAddress = ipAddress;
        setAuthenticated(false);
    }

    // 인증 후
    public JwtAuthenticationToken(TokenClaims claims, String ipAddress, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = null;
        this.ipAddress = ipAddress;
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return claims;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
