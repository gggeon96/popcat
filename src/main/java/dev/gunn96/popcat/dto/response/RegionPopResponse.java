package dev.gunn96.popcat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class RegionPopResponse {
    private String regionCode;
    private long count;
}
