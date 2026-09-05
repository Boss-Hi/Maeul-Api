package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourService {
    private final TourRepository tourRepository;

    public Tour findByContentId(String contentId) {
        return tourRepository.findByContentId(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Tour을 찾을 수 없습니다: " + contentId));
    }
}
