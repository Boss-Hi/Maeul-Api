package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.MissionTrigger;
import com.bosshi.maeul.itinerary.type.MissionTriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionTriggerRepository extends JpaRepository<MissionTrigger, Long> {
    // 특정 트리거 타입과 값(Category 코드 등) 리스트로 트리거들을 조회
    List<MissionTrigger> findByTypeAndValueIn(MissionTriggerType type, List<String> values);
}

