package dev.gunn96.popcat.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class IpAddressUtil {
    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "X-Real-IP",
            "REMOTE_ADDR"
    );

    public static String extractIpAddress(HttpServletRequest request) {
        return IP_HEADERS.stream()
                .map(request::getHeader)
                .filter(StringUtils::hasText)
                .map(ipList -> ipList.split(",")[0].trim())
                .filter(IpAddressUtil::isValidIpAddress)
                .findFirst()
                .orElseGet(request::getRemoteAddr);
    }

    private static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty() || ip.equalsIgnoreCase("unknown")) {
            return false;
        }

        return ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$") || // IPv4
                ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"); // IPv6
    }
}