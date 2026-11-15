package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.FatSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FatSettingRepository extends JpaRepository<FatSetting, Long> {

    Optional<FatSetting> findFirstByDeletedAtIsNullOrderByIdAsc();
}
