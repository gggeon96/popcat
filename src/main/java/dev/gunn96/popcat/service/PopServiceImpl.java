package dev.gunn96.popcat.service;

import dev.gunn96.popcat.domain.Pop;
import dev.gunn96.popcat.dto.response.PopResponse;
import dev.gunn96.popcat.mapper.PopMapper;
import dev.gunn96.popcat.repository.RegionPopRepository;
import dev.gunn96.popcat.repository.VisitorPopRepository;
import dev.gunn96.popcat.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class PopServiceImpl implements PopService {
    private final VisitorPopRepository visitorPopRepository;
    private final RegionPopRepository regionPopRepository;
    private final JwtProvider jwtProvider;
    private final PopMapper popMapper;

    @Value("${popcat.max-pops-append-per-visitor:800}")
    private long maxPopsAppendPerVisitor;

    @Transactional
    public PopResponse addPops(String ipAddress, String regionCode, long count) {
        long validCount = validateCount(count);
        updateVisotorPop(ipAddress, regionCode, validCount);
        updateRegionPop(regionCode, validCount);
        String newToken = jwtProvider.generateToken(ipAddress, regionCode);

        return popMapper.toResponse(validCount, newToken, true);
    }

    // 방문자 팝 업데이트
    private void updateVisotorPop(String ipAddress, String regionCode, long validCount) {
        Pop visitorPop = visitorPopRepository
                .findByIpAddressAndRegionCode(ipAddress, regionCode)
                .map(popMapper::from)
                .orElseGet(() -> Pop.createNew(ipAddress, regionCode));

        Pop updatedVisitorPop = visitorPop.addCount(validCount);
        visitorPopRepository.save(popMapper.toVisitorEntity(updatedVisitorPop));
    }

    // 지역 팝 업데이트
    private void updateRegionPop(String regionCode, long validCount) {
        Pop regionPop = regionPopRepository
                .findById(regionCode)
                .map(popMapper::from)
                .orElseGet(() -> Pop.createNew(null, regionCode));

        Pop updatedRegionPop = regionPop.addCount(validCount);
        regionPopRepository.save(popMapper.toRegionEntity(updatedRegionPop));
    }


    private long validateCount(long count) {
        if (count < 0) {
            return 0;
        }
        return Math.min(count, maxPopsAppendPerVisitor);
    }
}
