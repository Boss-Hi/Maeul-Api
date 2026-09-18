package com.bosshi.maeul.location.dto;

import lombok.*;
import org.springframework.modulith.NamedInterface;

@NamedInterface
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDTO {
    private String region1depth;
    private String region2depth;
}
