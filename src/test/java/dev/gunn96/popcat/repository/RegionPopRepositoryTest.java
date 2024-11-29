package dev.gunn96.popcat.repository;


import dev.gunn96.popcat.entity.RegionPopEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RegionPopRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private RegionPopRepository regionPopRepository;

    @Test
    @DisplayName("국가별 클릭수 저장 및 조회")
    void saveAndFind() {
        // given
        RegionPopEntity regionPop = RegionPopEntity.builder()
                .regionCode("KR")
                .count(100)
                .build();

        // when
        regionPopRepository.save(regionPop);
        RegionPopEntity found = regionPopRepository.findById("KR").orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getRegionCode()).isEqualTo("KR");
        assertThat(found.getCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("국가별 클릭수 업데이트")
    void update() {
        // given
        RegionPopEntity regionPop = RegionPopEntity.builder()
                .regionCode("KR")
                .count(100)
                .build();
        regionPopRepository.save(regionPop);

        // when
        RegionPopEntity saved = regionPopRepository.findById("KR").orElseThrow();
        RegionPopEntity updated = RegionPopEntity.builder()
                .regionCode(saved.getRegionCode())
                .count(200)
                .build();
        regionPopRepository.save(updated);

        // then
        RegionPopEntity found = regionPopRepository.findById("KR").orElseThrow();
        assertThat(found.getCount()).isEqualTo(200);
    }
}