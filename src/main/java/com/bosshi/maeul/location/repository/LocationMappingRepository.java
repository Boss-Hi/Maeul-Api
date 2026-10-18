package com.bosshi.maeul.location.repository;

import com.bosshi.maeul.location.entity.LocationMapping;
import com.bosshi.maeul.location.type.LocationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationMappingRepository extends JpaRepository<LocationMapping, Long> {
    Optional<LocationMapping> findByTargetAndTargetId(LocationTargetType target, Long targetId);
}

