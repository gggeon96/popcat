package dev.gunn96.popcat.controller;


import dev.gunn96.popcat.common.ApiResponse;
import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.security.jwt.TokenClaims;
import dev.gunn96.popcat.service.PopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/pop")
public class PopController {
    private final PopService popService;

    @PostMapping
    public ApiResponse<PopResponse> addPops(
            @RequestParam("count") Long count,
            @AuthenticationPrincipal TokenClaims claims
    ) {
        log.info("Add pops with count {}", count);
        PopResponse response = popService.addPops(claims.audience(), claims.subject(), count);
        log.info("response: {}", response);
        return ApiResponse.success(response);
    }
}


