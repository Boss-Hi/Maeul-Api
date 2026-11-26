package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.StoreOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreOwnerRepository extends JpaRepository<StoreOwner, Long> {
    java.util.Optional<StoreOwner> findByUserIdAndDeletedAtIsNull(Long userId);

    java.util.List<StoreOwner> findByStoreIdAndDeletedAtIsNull(Long storeId);
}
