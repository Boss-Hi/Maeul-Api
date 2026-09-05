package com.bosshi.maeul.survey.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.survey.type.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SurveyQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_badge", length = 32)
    private String stepBadge;

    @Column(nullable = false)
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 16)
    private QuestionType questionType;

    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    @Builder.Default
    private List<SurveyQuestionOption> options = new ArrayList<>();
}