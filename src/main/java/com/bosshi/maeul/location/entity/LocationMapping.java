package com.bosshi.maeul.location.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.location.type.LocationTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.modulith.NamedInterface;

@NamedInterface

@Entity
@Table(
        name = "location_mapping_tables",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_location_mapping_target", columnNames = {"target", "target_id"})
        },
        indexes = {
                @Index(name = "idx_location_mapping_target", columnList = "target,target_id"),
                @Index(name = "idx_location_mapping_lat_lng", columnList = "latitude,longitude")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LocationMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LocationTargetType target;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "address_line1", length = 256)
    private String addressLine1;

    @Column(name = "address_line2", length = 256)
    private String addressLine2;

    @Column(length = 16)
    private String zipcode;

    private Double latitude;

    private Double longitude;

    @Column(name = "region1_depth", length = 64)
    private String region1Depth;

    @Column(name = "region2_depth", length = 64)
    private String region2Depth;
}

