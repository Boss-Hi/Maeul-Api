package com.bosshi.maeul.openapi.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.modulith.NamedInterface;

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
     * 한국관광공사 API의 contenttypeid 국문 코드
     * 예: "15" (축제/공연), "12" (체험관광), "32" (숙박), "39" (음식)
     */
    @Column(nullable = false, length = 16)
    private String contentTypeId;

    /**
     * 한국관광공사 API의 contenttypeid 다국어 코드
     * 예: "15" (축제/공연), "12" (체험관광), "32" (숙박), "39" (음식)
     */
    @Column(length = 16)
    private String contentTypeIdMultiLang;

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
}
