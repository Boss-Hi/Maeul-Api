package com.bosshi.maeul.ai.entity;

import com.bosshi.maeul.ai.type.AiProviderType;
import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;

@NamedInterface
@Entity
@Table(name = "ai_api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 키 식별용 이름 (예: "A key", "B key", "개발용 Gemini 키")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // AI 제공자 유형 (예: GEMINI, GPT, CLAUDE)
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private AiProviderType providerType;

    @Column(name = "api_key", nullable = false, unique = true, length = 255)
    private String apiKey;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
