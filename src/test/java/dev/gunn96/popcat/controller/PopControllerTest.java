package dev.gunn96.popcat.controller;

import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.security.SecurityConfig;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationProvider;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationToken;
import dev.gunn96.popcat.security.jwt.JwtProvider;
import dev.gunn96.popcat.security.jwt.TokenClaims;
import dev.gunn96.popcat.service.GeoIpService;
import dev.gunn96.popcat.service.PopService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PopController.class)
@Import(SecurityConfig.class)
class PopControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PopService popService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private GeoIpService geoIpService;

    @MockitoBean
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Test
    @DisplayName("유효한 pop요청시 성공적으로 popResponse반환")
    void testPopApi() throws Exception {
        // given
        long count = 10L;
        String ipAddress = "127.0.0.1";
        String regionCode = "KR";
        String token = "valid.jwt.token";

        TokenClaims claims = TokenClaims.builder()
                .id("test-id")
                .issuer("test-issuer")
                .ipAddress(ipAddress)
                .regionCode(regionCode)
                .issuedAt(0L)
                .notBefore(0L)
                .expiresAt(999999999L)
                .build();

        PopResponse response = PopResponse.builder()
                .countAppend(count)
                .newToken("new.jwt.token")
                .isProcessed(true)
                .build();

        // Mock JWT validation and authentication
        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token, ipAddress);
        JwtAuthenticationToken authResult = new JwtAuthenticationToken(claims, ipAddress, Collections.emptyList());

        given(jwtAuthenticationProvider.supports(JwtAuthenticationToken.class)).willReturn(true);
        given(jwtAuthenticationProvider.authenticate(any(JwtAuthenticationToken.class))).willReturn(authResult);

        given(jwtProvider.validateToken(anyString(), anyString())).willReturn(claims);
        given(geoIpService.findRegionCodeByIpAddress(anyString())).willReturn(regionCode);
        given(popService.addPops(anyString(), anyString(), any(Long.class))).willReturn(response);

        // when & then
        mvc.perform(post("/api/v1/pop")
                        .header("Authorization", "Bearer " + token)
                        .param("count", String.valueOf(count)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.countAppend").value(count))
                .andExpect(jsonPath("$.data.newToken").value("new.jwt.token"))
                .andExpect(jsonPath("$.data.isProcessed").value(true));
    }
}
