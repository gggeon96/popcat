package dev.gunn96.popcat.dto.response;

import lombok.Builder;

@Builder
public record PopResponse(
        Long countAppend,
        String newToken,
        Boolean isProcessed) {
}
