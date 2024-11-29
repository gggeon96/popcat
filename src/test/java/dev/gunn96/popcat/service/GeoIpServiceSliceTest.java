package dev.gunn96.popcat.service;

import dev.gunn96.popcat.exception.GeoIpException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {GeoIpServiceImpl.class})
@TestPropertySource(properties = {
        "geoip.database.path=classpath:geoip/GeoLite2-Country.mmdb"
})
class GeoIpServiceSliceTest {

    @Autowired
    private GeoIpServiceImpl geoIpService;

    @Test
    @DisplayName("실제 GeoIP DB를 사용하여 국가 코드를 조회한다")
    void shouldResolveCountryCodesWithRealDatabase() {
        // given
        var testCases = new String[][] {
                {"8.8.8.8", "US"},           // Google DNS (미국)
                {"223.130.195.95", "KR"},    // Naver (한국)
                {"203.104.153.1", "JP"},     // NTT (일본)
                {"212.58.246.1", "GB"},      // BBC (영국)
                {"202.196.224.1", "CN"},     // China Telecom (중국)
                {"95.163.36.1", "RU"}        // Yandex (러시아)
        };

        // when & then
        for (String[] testCase : testCases) {
            String ipAddress = testCase[0];
            String expectedCountryCode = testCase[1];

            String actualCountryCode = geoIpService.findRegionCodeByIpAddress(ipAddress);

            assertThat(actualCountryCode)
                    .as("IP %s should resolve to country code %s", ipAddress, expectedCountryCode)
                    .isEqualTo(expectedCountryCode);
        }
    }

    @Test
    @DisplayName("잘못된 형식의 IP 주소에 대해 예외를 던진다")
    void shouldThrowExceptionForInvalidIpFormat() {
        // given
        String invalidIp = "invalid.ip.address";

        // when & then
        assertThatThrownBy(() -> geoIpService.findRegionCodeByIpAddress(invalidIp))
                .isInstanceOf(GeoIpException.InvalidIpAddressException.class)
                .hasMessageContaining("Invalid IP address format")
                .hasMessageContaining(invalidIp);
    }

    @Test
    @DisplayName("사설 IP 주소는 UNKNOWN을 반환한다")
    void shouldReturnUnknownForPrivateIp() {
        // given
        String privateIp = "192.168.0.1";

        // when
        String countryCode = geoIpService.findRegionCodeByIpAddress(privateIp);

        // then
        assertThat(countryCode).isEqualTo("UNKNOWN");
    }
}