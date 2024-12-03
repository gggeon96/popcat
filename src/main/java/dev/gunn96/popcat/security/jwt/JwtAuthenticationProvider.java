package dev.gunn96.popcat.security.jwt;

import dev.gunn96.popcat.service.GeoIpService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtProvider jwtProvider;
    private final GeoIpService geoIpService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Unsupported authentication type");
        }

        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
        String token = (String) jwtAuthentication.getCredentials();
        String ipAddress = jwtAuthentication.getIpAddress();

        try {
            String regionCode = geoIpService.findRegionCodeByIpAddress(ipAddress);
            TokenClaims claims = jwtProvider.validateToken(token, ipAddress, regionCode);
            return new JwtAuthenticationToken(claims, ipAddress, Collections.emptyList());
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid JWT token", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}