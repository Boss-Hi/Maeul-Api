package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    java.util.List<Store> findByDeletedAtIsNullOrderByIdAsc();

    /** 지워진 행도 센다 */
    boolean existsByName(String name);

    java.util.Optional<Store> findFirstByNameAndDeletedAtIsNullOrderByIdAsc(String name);
}
