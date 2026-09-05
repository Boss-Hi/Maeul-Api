package com.bosshi.maeul.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.modulith.NamedInterface;

@NamedInterface
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LocationDTO {
    private String region1depthName;
    private String region2depthName;
}
