package dev.gunn96.popcat.common;

import dev.gunn96.popcat.entity.VisitorPopEntity;
import dev.gunn96.popcat.repository.VisitorPopRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("JPA Auditing 테스트")
class JpaConfigTest {

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
    private VisitorPopRepository visitorPopRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("엔티티 생성 시 생성일시와 수정일시가 자동 설정됨")
    void createAuditingTest() {
        // given
        VisitorPopEntity visitorPop = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(50)
                .build();

        // when
        VisitorPopEntity saved = visitorPopRepository.save(visitorPop);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualToIgnoringNanos(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("엔티티 수정 시 수정일시만 업데이트됨")
    @Transactional
    void updateAuditingTest() throws InterruptedException {
        // given
        VisitorPopEntity visitorPop = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(50)
                .build();

        VisitorPopEntity saved = visitorPopRepository.save(visitorPop);
        entityManager.flush();
        entityManager.clear();  // 영속성 컨텍스트 초기화

        saved = visitorPopRepository.findById(new VisitorPopEntity.VisitorPopId(saved.getIpAddress(), saved.getRegionCode()))
                .orElseThrow();
        LocalDateTime createdAt = saved.getCreatedAt();
        LocalDateTime updatedAt = saved.getUpdatedAt();

        Thread.sleep(100);  // 시간 차이를 확인하기 위한 대기

        // when
        saved.updateCount(100); // 엔티티의 상태를 변경
        VisitorPopEntity result = visitorPopRepository.save(saved);
        entityManager.flush();
        entityManager.refresh(result);  // 엔티티 상태 새로고침

        // then
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isAfter(updatedAt);
    }

    @Test
    @DisplayName("기본값 테스트 - isDeleted는 false로 설정됨")
    void defaultValueTest() {
        // given
        VisitorPopEntity visitorPop = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(50)
                .build();

        // when
        VisitorPopEntity saved = visitorPopRepository.save(visitorPop);

        // then
        assertThat(saved.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("soft delete 설정 가능")
    void softDeleteTest() {
        // given
        VisitorPopEntity visitorPop = VisitorPopEntity.builder()
                .ipAddress("192.168.1.1")
                .regionCode("KR")
                .count(50)
                .isDeleted(true)
                .build();

        // when
        VisitorPopEntity saved = visitorPopRepository.save(visitorPop);

        // then
        assertThat(saved.isDeleted()).isTrue();
    }
}