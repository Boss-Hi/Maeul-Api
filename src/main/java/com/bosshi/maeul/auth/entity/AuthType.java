package com.bosshi.maeul.auth.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Table(name = "auth_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthType extends BaseEntity {
    @Id
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

}
