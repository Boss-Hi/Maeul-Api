package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.kraftadmin.annotations.KraftAdminField;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 퀴즈로 보여 주는 기준 사진 */
@Entity
@Table(name = "photo_mission_reference_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PhotoMissionReferencePhoto extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    /** Seeder 가 중복 생성하지 않도록 구분하는 코드 (없을 수 있다) */
    @Column(length = 64)
    private String code;

    /** URL 로 등록한 기준 사진. imageBase64 가 있으면 쓰지 않는다 */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "mime_type", length = 32)
    private String mimeType;

    /** 이미지 파일의 base64. 서버가 직접 서빙하고 AI 로 보낸다 */
    @KraftAdminField(showInTable = false)
    @Column(name = "image_base64", columnDefinition = "MEDIUMTEXT")
    private String imageBase64;

    @Column(nullable = false)
    private boolean enabled;
}
