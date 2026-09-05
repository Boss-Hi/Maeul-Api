package com.bosshi.maeul.openapi.response;

import com.bosshi.maeul.openapi.entity.Tour;

public record TourResponse(
        String contentId,
        String title,
        String tel,
        String addr1,
        String addr2,
        String zipcode,
        String firstImage,
        String firstImage2,
        Double mapX,
        Double mapY,
        String createdTime,
        String modifiedTime
) {

    public static TourResponse from(Tour tour) {
        return new TourResponse(
                tour.getContentId(),
                tour.getTitle(),
                tour.getTel(),
                tour.getAddr1(),
                tour.getAddr2(),
                tour.getZipcode(),
                tour.getFirstImage(),
                tour.getFirstImage2(),
                tour.getMapX(),
                tour.getMapY(),
                tour.getCreatedTime(),
                tour.getModifiedTime()
        );
    }
}
