package com.bosshi.maeul.category.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;

@NamedInterface
@Entity
@Table(name = "tour_content_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TourContentType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 카테고리 코드
     * 예: "AC" (숙박), "EV" (축제/공연), "EX" (체험관광), "FD" (음식)
     */
    @Column(nullable = false, unique = true, length = 4)
    private String code;

    /**
     * 한국관광공사 API의 contenttypeid 다국어 코드
     * 예: "15" (축제/공연), "12" (체험관광), "32" (숙박), "39" (음식)
     */
    @Column(nullable = false, length = 4)
    private String codeMultiLang;

    /**
     * 카테고리명
     * 예: "축제/공연", "체험관광/공예", "숙박/호텔", "음식"
     */
    @Column(nullable = false, length = 32)
    private String name;

    /**
     * 사용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
