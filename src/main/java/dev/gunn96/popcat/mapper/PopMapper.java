package dev.gunn96.popcat.mapper;

import dev.gunn96.popcat.domain.Pop;
import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.entity.RegionPopEntity;
import dev.gunn96.popcat.entity.VisitorPopEntity;
import org.springframework.stereotype.Component;

@Component
public class PopMapper {
    public Pop from(VisitorPopEntity entity) {
        return Pop.builder()
                .ipAddress(entity.getIpAddress())
                .regionCode(entity.getRegionCode())
                .count(entity.getCount())
                .build();
    }

    public Pop from(RegionPopEntity entity) {
        return Pop.builder()
                .regionCode(entity.getRegionCode())
                .count(entity.getCount())
                .build();
    }

    public VisitorPopEntity toVisitorEntity(Pop pop) {
        return VisitorPopEntity.builder()
                .ipAddress(pop.getIpAddress())
                .regionCode(pop.getRegionCode())
                .count(pop.getCount())
                .build();
    }

    public RegionPopEntity toRegionEntity(Pop pop) {
        return RegionPopEntity.builder()
                .regionCode(pop.getRegionCode())
                .count(pop.getCount())
                .build();
    }

    public PopResponse toResponse(long countAppend, String newToken) {
        return new PopResponse(countAppend, newToken);
    }
}
