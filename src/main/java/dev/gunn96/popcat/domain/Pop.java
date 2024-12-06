package dev.gunn96.popcat.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class Pop {
    private final String ipAddress;
    private final String regionCode;
    private final long count;

    public static Pop createNew(String ipAddress, String regionCode) {
        return Pop.builder()
                .ipAddress(ipAddress)
                .regionCode(regionCode)
                .count(0L)
                .build();
    }

    public Pop addCount(long additionalCount) {
        return Pop.builder()
                .ipAddress(this.ipAddress)
                .regionCode(this.regionCode)
                .count(this.count + additionalCount)
                .build();
    }
}