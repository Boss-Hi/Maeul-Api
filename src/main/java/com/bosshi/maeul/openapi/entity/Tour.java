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
    @Column(name = "content_id", nullable = false, length = 32)
    private String contentId;

    @Column(name = "content_type_id", length = 16)
    private String contentTypeId;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "tel", length = 128)
    private String tel;

    @Column(name = "telname", length = 128)
    private String telname;

    @Column(name = "addr1", length = 256)
    private String addr1;

    @Column(name = "addr2", length = 256)
    private String addr2;

    @Column(name = "zipcode", length = 16)
    private String zipcode;

    @Column(name = "area_code", length = 16)
    private String areaCode;

    @Column(name = "sigungu_code", length = 16)
    private String sigunguCode;

    @Column(name = "cat1", length = 16)
    private String cat1;

    @Column(name = "cat2", length = 16)
    private String cat2;

    @Column(name = "cat3", length = 16)
    private String cat3;

    @Column(name = "event_start_date", length = 16)
    private String eventStartDate;

    @Column(name = "event_end_date", length = 16)
    private String eventEndDate;

    @Column(name = "first_image", length = 512)
    private String firstImage;

    @Column(name = "first_image2", length = 512)
    private String firstImage2;

    @Column(name = "cpyrht_div_cd", length = 32)
    private String cpyrhtDivCd;

    @Column(name = "map_x")
    private Double mapX;

    @Column(name = "map_y")
    private Double mapY;

    @Column(name = "m_level", length = 8)
    private String mLevel;

    @Column(name = "l_dong_regn_cd", length = 16)
    private String lDongRegnCd;

    @Column(name = "l_dong_signgu_cd", length = 16)
    private String lDongSignguCd;

    @Column(name = "lcls_systm1", length = 32)
    private String lclsSystm1;

    @Column(name = "lcls_systm2", length = 32)
    private String lclsSystm2;

    @Column(name = "lcls_systm3", length = 32)
    private String lclsSystm3;

    @Column(name = "progress_type", length = 32)
    private String progressType;

    @Column(name = "festival_type", length = 32)
    private String festivalType;

    @Column(name = "created_time", length = 16)
    private String createdTime;

    @Column(name = "modified_time", length = 16)
    private String modifiedTime;

    @Column(name = "sponsor1", length = 128)
    private String sponsor1;

    @Column(name = "sponsor1tel", length = 128)
    private String sponsor1tel;

    @Column(name = "sponsor2", length = 128)
    private String sponsor2;

    @Column(name = "sponsor2tel", length = 128)
    private String sponsor2tel;

    @Column(name = "playtime", length = 128)
    private String playtime;

    @Column(name = "eventplace", length = 128)
    private String eventplace;

    @Column(name = "eventhomepage", length = 512)
    private String eventhomepage;

    @Column(name = "agelimit", length = 16)
    private String agelimit;

    @Column(name = "bookingplace", length = 128)
    private String bookingplace;

    @Column(name = "placeinfo", length = 2)
    private String placeinfo;

    @Column(name = "subevent", length = 2)
    private String subevent;

    @Column(name = "usetimefestival", length = 512)
    private String usetimefestival;

    @Column(name = "discountinfofestival", length = 2)
    private String discountinfofestival;

    @Column(name = "spendtimefestival", length = 2)
    private String spendtimefestival;

    @Column(name = "festivalgrade", length = 2)
    private String festivalgrade;

    // SQL 스키마에 `progress_type`과 별도로 존재하는 `progresstype` 매핑
    @Column(name = "progresstype", length = 2)
    private String progresstype;

    // SQL 스키마에 `festival_type`과 별도로 존재하는 `festivaltype` 매핑
    @Column(name = "festivaltype", length = 2)
    private String festivaltype;

    @Column(name = "homepage", length = 512)
    private String homepage;

    @Column(name = "program", length = 2048)
    private String program;

    @Column(name = "overview", length = 2048)
    private String overview;

}