package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.PrizeVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PrizeVoucherRepository extends JpaRepository<PrizeVoucher, Long> {

    List<PrizeVoucher> findByUserIdOrderByIdDesc(Long userId);

    List<PrizeVoucher> findByGachaResultIdIn(Collection<Long> gachaResultIds);
}
