package dev.gunn96.popcat.controller;

import dev.gunn96.popcat.common.ApiResponse;
import dev.gunn96.popcat.dto.response.LeaderboardResponse;
import dev.gunn96.popcat.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    @GetMapping
    public ApiResponse<LeaderboardResponse> getLeaderboard() {
        log.info("Get leaderboard");
        return ApiResponse.success(leaderboardService.getLeaderboard());
    }

}
