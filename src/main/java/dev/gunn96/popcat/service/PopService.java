package dev.gunn96.popcat.service;

import dev.gunn96.popcat.dto.response.PopResponse;

public interface PopService {
    PopResponse addPops(String ipAddress, String regionCode, long count);
}
