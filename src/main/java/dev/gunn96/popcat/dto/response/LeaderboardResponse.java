package dev.gunn96.popcat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private long globalSum;
    @JsonProperty("rankingList")
    private List<RegionPopResponse> rankingList;
}
