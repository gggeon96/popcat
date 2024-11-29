package dev.gunn96.popcat.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

@Entity
@Table(name = "visitor_pops")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VisitorPopEntity.VisitorPopId.class)
@EntityListeners(AuditingEntityListener.class)
public class VisitorPopEntity extends BaseEntity {
    @Id
    private String ipAddress;

    @Id
    private String regionCode;

    private long count;

    //TODO 동시성 문제 발생 원인. MVP 구현 후 해결하기
    public void updateCount(long count) {
        this.count = count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitorPopId implements Serializable {
        private String ipAddress;
        private String regionCode;
    }
}