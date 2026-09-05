package com.bosshi.maeul.survey.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "survey_question_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SurveyQuestionOption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(nullable = false, length = 128)
    private String label;

    @Column(name = "sub_label")
    private String subLabel;

    @Column(nullable = false, length = 32)
    private String value;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;
}
