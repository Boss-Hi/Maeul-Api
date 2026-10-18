package com.bosshi.maeul.itinerary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "itinerary_schedule_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryScheduleType {

    @Id
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;
}

