package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.entity.GachaOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GachaOutcomeRepository extends JpaRepository<GachaOutcome, Long> {

    List<GachaOutcome> findByGachaTypeAndDeletedAtIsNullOrderByIdAsc(GachaType gachaType);
}
