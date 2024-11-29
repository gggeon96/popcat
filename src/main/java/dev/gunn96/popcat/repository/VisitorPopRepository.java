package dev.gunn96.popcat.repository;

import dev.gunn96.popcat.entity.VisitorPopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorPopRepository extends JpaRepository<VisitorPopEntity, VisitorPopEntity.VisitorPopId> {
}
