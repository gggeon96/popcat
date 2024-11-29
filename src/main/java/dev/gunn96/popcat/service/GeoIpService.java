package dev.gunn96.popcat.service;

public interface GeoIpService {
    String findRegionCodeByIpAddress(String ipAddress);
}
