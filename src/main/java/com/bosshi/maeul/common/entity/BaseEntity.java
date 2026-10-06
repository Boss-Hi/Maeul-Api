package com.bosshi.maeul.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;

@NamedInterface
@MappedSuperclass
@EnableJpaAuditing
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

    @PrePersist
    protected void prePersist() {
        /*LocalDateTime now = now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;*/
    }

    @PreUpdate
    protected void preUpdate() {
        /*this.updatedAt = now();*/
    }

    public void softDelete() {
        this.deletedAt = now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

}
