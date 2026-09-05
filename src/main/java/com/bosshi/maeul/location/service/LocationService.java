package com.bosshi.maeul.location.service;

import com.bosshi.maeul.location.dto.LocationDTO;
import com.bosshi.maeul.location.impl.AddressResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final AddressResolver addressResolver;

    public LocationDTO getRegionAddress(Double latitude, Double longitude) {
        return addressResolver.getRegionAddress(latitude, longitude);
    }
}