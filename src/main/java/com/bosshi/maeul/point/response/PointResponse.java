package com.bosshi.maeul.point.response;

import com.bosshi.maeul.point.entity.PointTransaction;
import com.bosshi.maeul.point.type.PointReason;

import java.time.LocalDateTime;
import java.util.List;

public record PointResponse(long balance, List<Transaction> transactions) {

    public record Transaction(Long id, long amount, PointReason reason, LocalDateTime createdAt) {
        public static Transaction from(PointTransaction transaction) {
            return new Transaction(transaction.getId(), transaction.getAmount(), transaction.getReason(), transaction.getCreatedAt());
        }
    }
}
