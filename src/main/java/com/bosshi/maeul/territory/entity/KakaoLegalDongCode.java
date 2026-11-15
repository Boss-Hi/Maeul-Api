package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 카카오가 위치를 판정할 때 돌려주는 법정동 코드의 시군구 1개. 미리 채워 두며 region_subdivisions 와는 연결하지 않는다.
 */
@Entity
@Table(
        name = "kakao_legal_dong_codes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_kakao_legal_dong_codes_code", columnNames = {"code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class KakaoLegalDongCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 시군구 법정동 코드 (카카오 법정동 코드 앞 5자리). 점령의 regionId 와 같은 값이다. */
    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "sido_name", nullable = false, length = 32)
    private String sidoName;

    /** 시군구 이름. 세종특별자치시처럼 시군구가 없으면 null */
    @Column(name = "sigungu_name", length = 32)
    private String sigunguName;
}
