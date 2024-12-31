package dev.gunn96.popcat.controller;

import dev.gunn96.popcat.security.SecurityConfig;
import dev.gunn96.popcat.security.jwt.JwtAuthenticationProvider;
import dev.gunn96.popcat.security.jwt.JwtProvider;
import dev.gunn96.popcat.service.GeoIpService;
import dev.gunn96.popcat.service.LeaderboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaderboardController.class)
@Import({SecurityConfig.class, JwtAuthenticationProvider.class})
public class LeaderboardControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @MockitoBean
    private GeoIpService geoIpService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("leaderboard를 조회하는 API를 호출하면 응답을한다")
    void whenLeaderboardEndpointIsCalledItReturnsSuccessfulResponse() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/leaderboard"))
                .andExpect(status().isOk());
    }
}