package com.bosshi.maeul.plan.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

/**
 * 여행의 중심이 되는 축제/공연을 나타내는 도메인 모델
 *
 * MainFestival은 여행의 지리적, 시간적 중심점이 되어 다른 관광지 추천의 기준이 됩니다.
 */
@Entity
@Table(name = "main_festivals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MainFestival extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 한국관광공사 API의 contentId
     */
    @Column(nullable = false, unique = true)
    private String contentId;

    /**
     * 축제명
     */
    @Column(nullable = false)
    private String title;

    /**
     * 축제/공연 카테고리
     */
    @Column(nullable = false)
    private String category;

    /**
     * 축제 개최 시작일
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * 축제 개최 종료일
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * 위도 (mapx)
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * 경도 (mapy)
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * 주소1
     */
    @Column(length = 500)
    private String address1;

    /**
     * 주소2
     */
    @Column(length = 500)
    private String address2;

    /**
     * 전화번호
     */
    @Column(length = 50)
    private String tel;

    /**
     * 이미지 URL
     */
    @Column(length = 1000)
    private String imageUrl;

    /**
     * 지역 코드
     */
    @Column(length = 10)
    private String areaCode;

            /**
     * 두 지점 사이의 거리를 Haversine 공식으로 계산합니다.
     *
     * @param lat 목표 위도
     * @param lon 목표 경도
     * @return 거리 (km)
     */
    public Double calculateDistance(Double lat, Double lon) {
        final int EARTH_RADIUS = 6371; // km

        Double dLat = Math.toRadians(lat - this.latitude);
        Double dLon = Math.toRadians(lon - this.longitude);

        Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}

