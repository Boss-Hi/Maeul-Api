package com.bosshi.maeul.location.impl;

import com.bosshi.maeul.location.dto.LocationDTO;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface AddressResolver {
    LocationDTO getRegionAddress(Double latitude, Double longitude);
}
