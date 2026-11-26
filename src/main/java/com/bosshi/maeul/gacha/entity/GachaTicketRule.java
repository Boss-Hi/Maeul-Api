package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.gacha.TicketEventType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 이벤트 종류별 가챠권 발급 설정. 바꾼 값은 그 뒤 발급분부터 적용된다 */
@Entity
@Table(name = "gacha_ticket_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GachaTicketRule extends BaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 32)
    private TicketEventType eventType;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "validity_days", nullable = false)
    private int validityDays;
}
