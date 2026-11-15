package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.KakaoLegalDongCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KakaoLegalDongCodeRepository extends JpaRepository<KakaoLegalDongCode, Long> {

    List<KakaoLegalDongCode> findByDeletedAtIsNullOrderByCodeAsc();

    List<KakaoLegalDongCode> findByCodeInAndDeletedAtIsNull(Collection<String> codes);
}
