package dev.gunn96.popcat.controller;

import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.security.SecurityConfig;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationProvider;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationToken;
import dev.gunn96.popcat.security.jwt.JwtProvider;
import dev.gunn96.popcat.security.jwt.TokenClaims;
import dev.gunn96.popcat.service.GeoIpService;
import dev.gunn96.popcat.service.PopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PopController.class)
@Import(SecurityConfig.class)
public class PopControllerTest {
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

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        given(authenticationManager.authenticate(any()))
                .willAnswer(invocation -> {
                    Authentication auth = invocation.getArgument(0);
                    return new JwtAuthenticationToken(
                            TokenClaims.builder()
                                    .audience("127.0.0.1")
                                    .subject("KR")
                                    .build(),
                            "127.0.0.1",
                            Collections.emptyList()
                    );
                });
    }

    @Test
    @DisplayName("유효한 pop요청시 성공적으로 popResponse반환")
    public void testPopApi() throws Exception {
        // given
        long count = 10L;
        String newToken = "new.jwt.token";
        PopResponse response = PopResponse.builder()
                .countAppend(count)
                .newToken(newToken)
                .build();

        given(popService.addPops(anyString(), anyString(), anyLong()))
                .willReturn(response);

        // when & then
        mvc.perform(post("/api/v1/pop")
                        .param("count", String.valueOf(count))
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.countAppend").value(count))
                .andExpect(jsonPath("$.data.newToken").value(newToken));
    }
}