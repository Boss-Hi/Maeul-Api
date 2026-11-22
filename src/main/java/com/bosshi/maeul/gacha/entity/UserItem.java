package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 사용자가 가진 아이템. 같은 아이템이 또 나오면 수량이 는다 */
@Entity
@Table(name = "user_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_items_user_item", columnNames = {"user_id", "item_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer quantity;
}
