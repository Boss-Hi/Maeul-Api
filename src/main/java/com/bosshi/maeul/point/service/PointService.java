package com.bosshi.maeul.point.service;

import com.bosshi.maeul.point.entity.PointTransaction;
import com.bosshi.maeul.point.repository.PointTransactionRepository;
import com.bosshi.maeul.point.type.PointReason;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointTransactionRepository repository;

    public long balance(Long userId) {
        return repository.sumAmountByUserId(userId);
    }

    public long count(Long userId, PointReason reason) {
        return repository.countByUserIdAndReason(userId, reason);
    }

    public List<PointTransaction> recent(Long userId, int size) {
        return repository.findByUserIdOrderByIdDesc(userId, PageRequest.of(0, size));
    }

    /**
     * 포인트 지급. 같은 refKey 로 이미 지급했으면 아무것도 하지 않는다.
     */
    @Transactional
    public void award(Long userId, long amount, PointReason reason, String refKey) {
        if (repository.existsByUserIdAndRefKey(userId, refKey)) {
            return;
        }
        repository.save(PointTransaction.builder()
                .userId(userId)
                .amount(amount)
                .reason(reason)
                .refKey(refKey)
                .build());
    }
}
