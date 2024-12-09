package dev.gunn96.popcat.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gunn96.popcat.common.ApiResponse;
import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.service.GeoIpService;
import dev.gunn96.popcat.util.IpAddressUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final GeoIpService geoIpService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractBearerTokenThatRemovedPrefix(request);
        String ipAddress = IpAddressUtil.extractIpAddress(request);

        // Case the token doesn't exist
        if (token == null) {
            handleNoToken(ipAddress, response);
            return;
        }

        //Case the token exists
        try {
            JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token, ipAddress);
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleExpiredToken(ipAddress, response);
        } catch (Exception e) {
            handleInvalidToken(response, "Invalid token");
        }
    }

    //if the token doesn't exist, publish new token.
    private void handleNoToken(String ipAddress, HttpServletResponse response) throws IOException {
        String regionCode = geoIpService.findRegionCodeByIpAddress(ipAddress);
        String newToken = jwtProvider.generateToken(ipAddress, regionCode);
        sendTokenResponse(response, newToken);
    }

    // if the token has expired, publish new token.
    private void handleExpiredToken(String ipAddress, HttpServletResponse response) throws IOException {
        String regionCode = geoIpService.findRegionCodeByIpAddress(ipAddress);
        String newToken = jwtProvider.generateToken(ipAddress, regionCode);
        sendTokenResponse(response, newToken);
    }

    // if thoe token is failed to validate
    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.error("INVALID_TOKEN", message));
    }

    private void sendTokenResponse(HttpServletResponse response, String token) throws IOException {
        PopResponse popResponse = PopResponse.builder()
                .countAppend(null)
                .newToken(token)
                .isProcessed(false)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.success(popResponse));
    }

    // removes `Bearer ` prefix
    private String extractBearerTokenThatRemovedPrefix(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}