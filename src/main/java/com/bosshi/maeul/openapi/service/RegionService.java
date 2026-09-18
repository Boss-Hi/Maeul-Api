package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.LDongRegion;
import com.bosshi.maeul.openapi.repository.LDongRegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final LDongRegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<LDongRegion> getRegionsWithSigungus() {
        return regionRepository.findAllWithSigungus();
    }
}
