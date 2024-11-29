package dev.gunn96.popcat.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import dev.gunn96.popcat.exception.GeoIpException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoIpServiceTest {

    @Mock
    private DatabaseReader reader;

    @InjectMocks
    private GeoIpServiceImpl geoIpService;

    @Test
    @DisplayName("정상적인 IP 주소에 대해 국가 코드를 반환한다")
    void shouldReturnCountryCodeForValidIp() throws Exception {
        // given
        String ipAddress = "1.1.1.1";
        String expectedCountryCode = "KR";

        CountryResponse countryResponse = mock(CountryResponse.class);
        Country country = mock(Country.class);

        when(country.getIsoCode()).thenReturn(expectedCountryCode);
        when(countryResponse.getCountry()).thenReturn(country);
        when(reader.tryCountry(any(InetAddress.class))).thenReturn(Optional.of(countryResponse));

        ReflectionTestUtils.setField(geoIpService, "reader", reader);

        // when
        String result = geoIpService.findRegionCodeByIpAddress(ipAddress);

        // then
        assertThat(result).isEqualTo(expectedCountryCode);
    }

    @Test
    @DisplayName("GeoIP DB에서 국가를 찾을 수 없을 때 UNKNOWN을 반환한다")
    void shouldReturnUnknownWhenCountryNotFound() throws Exception {
        // given
        String ipAddress = "1.1.1.1";
        when(reader.tryCountry(any(InetAddress.class))).thenReturn(Optional.empty());

        ReflectionTestUtils.setField(geoIpService, "reader", reader);

        // when
        String result = geoIpService.findRegionCodeByIpAddress(ipAddress);

        // then
        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("잘못된 IP 주소 형식에 대해 예외를 던진다")
    void shouldThrowExceptionForInvalidIpFormat() {
        // given
        String invalidIp = "invalid.ip.address";
        ReflectionTestUtils.setField(geoIpService, "reader", reader);

        // when & then
        assertThatThrownBy(() -> geoIpService.findRegionCodeByIpAddress(invalidIp))
                .isInstanceOf(GeoIpException.InvalidIpAddressException.class)
                .hasMessageContaining("Invalid IP address format")
                .hasMessageContaining(invalidIp);
    }

    @Test
    @DisplayName("데이터베이스 조회 중 예외 발생 시 DatabaseLookupException을 던진다")
    void shouldThrowExceptionWhenDatabaseLookupFails() throws Exception {
        // given
        String ipAddress = "1.1.1.1";
        when(reader.tryCountry(any(InetAddress.class))).thenThrow(new IOException("Database error"));
        ReflectionTestUtils.setField(geoIpService, "reader", reader);

        // when & then
        assertThatThrownBy(() -> geoIpService.findRegionCodeByIpAddress(ipAddress))
                .isInstanceOf(GeoIpException.DatabaseLookupException.class)
                .hasMessageContaining("Failed to lookup IP address")
                .hasMessageContaining(ipAddress);
    }
}