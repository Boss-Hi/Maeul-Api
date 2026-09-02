package com.bosshi.maeul.openapi.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tours")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tour extends BaseEntity {

    @Id
    @Column(name = "content_id", nullable = false, length = 20)
    private String contentId; // contentid (PK)

    @Column(name = "content_type_id", length = 10)
    private String contentTypeId; // contenttypeid

    @Column(name = "title", nullable = false)
    private String title; // title

    @Column(name = "tel", length = 50)
    private String tel; // tel

    @Column(name = "addr1")
    private String addr1; // addr1

    @Column(name = "addr2")
    private String addr2; // addr2

    @Column(name = "zipcode", length = 10)
    private String zipcode; // zipcode

    @Column(name = "area_code", length = 10)
    private String areaCode; // areacode

    @Column(name = "sigungu_code", length = 10)
    private String sigunguCode; // sigungucode

    @Column(name = "cat1", length = 10)
    private String cat1; // cat1

    @Column(name = "cat2", length = 10)
    private String cat2; // cat2

    @Column(name = "cat3", length = 10)
    private String cat3; // cat3

    @Column(name = "event_start_date")
    private String eventStartDate; // eventstartdate (예: "20260502" -> LocalDate)

    @Column(name = "event_end_date")
    private String eventEndDate; // eventenddate (예: "20260503" -> LocalDate)

    @Column(name = "first_image", length = 500)
    private String firstImage; // firstimage

    @Column(name = "first_image2", length = 500)
    private String firstImage2; // firstimage2

    @Column(name = "cpyrht_div_cd", length = 20)
    private String cpyrhtDivCd; // cpyrhtDivCd

    @Column(name = "map_x")
    private Double mapX; // mapx (경도)

    @Column(name = "map_y")
    private Double mapY; // mapy (위도)

    @Column(name = "m_level", length = 5)
    private String mLevel; // mlevel

    @Column(name = "l_dong_regn_cd", length = 10)
    private String lDongRegnCd; // lDongRegnCd

    @Column(name = "l_dong_signgu_cd", length = 10)
    private String lDongSignguCd; // lDongSignguCd

    @Column(name = "lcls_systm1", length = 20)
    private String lclsSystm1; // lclsSystm1

    @Column(name = "lcls_systm2", length = 20)
    private String lclsSystm2; // lclsSystm2

    @Column(name = "lcls_systm3", length = 20)
    private String lclsSystm3; // lclsSystm3

    @Column(name = "progress_type", length = 20)
    private String progressType; // progresstype

    @Column(name = "festival_type", length = 20)
    private String festivalType; // festivaltype

    @Column(name = "created_time")
    private String createdTime; // createdtime (예: "20240614132716" -> LocalDateTime)

    @Column(name = "modified_time")
    private String modifiedTime; // modifiedtime (예: "20260806144212" -> LocalDateTime)
}