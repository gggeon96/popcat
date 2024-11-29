package dev.gunn96.popcat.repository;

import dev.gunn96.popcat.entity.VisitorPopEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("VisitorPop Repository 테스트")
public class VisitorPopRepositoryTest {

    @Container
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
    private VisitorPopRepository visitorPopRepository;

    @Test
    @DisplayName("방문자 클릭수 저장 및 조회")
    void saveAndFind() {
        // given
        VisitorPopEntity visitorPop = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(50)
                .build();

        VisitorPopEntity.VisitorPopId id = VisitorPopEntity.VisitorPopId.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .build();

        // when
        visitorPopRepository.save(visitorPop);
        VisitorPopEntity found = visitorPopRepository.findById(id).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(found.getRegionCode()).isEqualTo("KR");
        assertThat(found.getCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("방문자 클릭수 업데이트")
    void update() {
        // given
        VisitorPopEntity visitorPop = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(50)
                .build();

        VisitorPopEntity.VisitorPopId id = VisitorPopEntity.VisitorPopId.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .build();

        visitorPopRepository.save(visitorPop);

        // when
        // setter 대신 새로운 객체를 생성하여 update
        VisitorPopEntity updated = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(100)
                .build();
        visitorPopRepository.save(updated);

        // then
        VisitorPopEntity found = visitorPopRepository.findById(id).orElseThrow();
        assertThat(found.getCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("존재하지 않는 방문자 조회시 Empty 반환")
    void notFound() {
        // given
        VisitorPopEntity.VisitorPopId id = VisitorPopEntity.VisitorPopId.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .build();

        // when
        Optional<VisitorPopEntity> result = visitorPopRepository.findById(id);

        // then
        assertThat(result).isEmpty();
    }
}