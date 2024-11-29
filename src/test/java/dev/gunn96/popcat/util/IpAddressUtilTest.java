package dev.gunn96.popcat.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class IpAddressUtilTest {

    @Test
    @DisplayName("X-Forwarded-For 헤더가 있을 때 첫 번째 IP를 추출한다")
    void shouldExtractFirstIpFromXForwardedFor() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String originalIp = "203.104.153.1";  // 원본 클라이언트 IP (일본)
        String proxyIp = "10.0.0.1";          // 프록시 서버 IP
        request.addHeader("X-Forwarded-For", originalIp + ", " + proxyIp);
        request.setRemoteAddr(proxyIp);

        // when
        String extractedIp = IpAddressUtil.extractIpAddress(request);

        // then
        assertThat(extractedIp).isEqualTo(originalIp);
    }

    @Test
    @DisplayName("X-Real-IP 헤더를 통해 실제 IP를 추출한다")
    void shouldExtractIpFromXRealIp() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String originalIp = "223.130.195.95";  // 원본 클라이언트 IP (한국)
        String proxyIp = "10.0.0.1";          // 프록시 서버 IP
        request.addHeader("X-Real-IP", originalIp);
        request.setRemoteAddr(proxyIp);

        // when
        String extractedIp = IpAddressUtil.extractIpAddress(request);

        // then
        assertThat(extractedIp).isEqualTo(originalIp);
    }

    @Test
    @DisplayName("여러 프록시를 거친 경우 원본 IP를 추출한다")
    void shouldExtractOriginalIpFromMultipleProxies() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String originalIp = "8.8.8.8";        // 원본 클라이언트 IP (미국)
        String proxy1Ip = "10.0.0.1";         // 첫 번째 프록시
        String proxy2Ip = "10.0.0.2";         // 두 번째 프록시
        String proxy3Ip = "10.0.0.3";         // 세 번째 프록시

        request.addHeader("X-Forwarded-For",
                String.join(", ", originalIp, proxy1Ip, proxy2Ip));
        request.setRemoteAddr(proxy3Ip);

        // when
        String extractedIp = IpAddressUtil.extractIpAddress(request);

        // then
        assertThat(extractedIp).isEqualTo(originalIp);
    }

    @Test
    @DisplayName("프록시 헤더가 없을 경우 RemoteAddr를 사용한다")
    void shouldUseRemoteAddrWhenNoProxyHeaders() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String directIp = "223.130.195.95";  // 직접 연결된 클라이언트 IP
        request.setRemoteAddr(directIp);

        // when
        String extractedIp = IpAddressUtil.extractIpAddress(request);

        // then
        assertThat(extractedIp).isEqualTo(directIp);
    }

    @Test
    @DisplayName("알 수 없는 IP 값이 포함된 경우 다음 헤더를 확인한다")
    void shouldSkipUnknownIpValue() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String originalIp = "203.104.153.1";  // 실제 IP
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("X-Real-IP", originalIp);
        request.setRemoteAddr("10.0.0.1");

        // when
        String extractedIp = IpAddressUtil.extractIpAddress(request);

        // then
        assertThat(extractedIp).isEqualTo(originalIp);
    }
}