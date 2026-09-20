package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.modulith.NamedInterface;

@NamedInterface
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroResponse extends OpenApiBaseResponse<DetailIntroResponse.Item> {

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String contentid;
        private String contenttypeid;
        private String sponsor1;
        private String sponsor1tel;
        private String sponsor2;
        private String sponsor2tel;
        private String eventenddate;
        private String playtime;
        private String eventplace;
        private String eventhomepage;
        private String agelimit;
        private String bookingplace;
        private String placeinfo;
        private String subevent;
        private String program;
        private String eventstartdate;
        private String usetimefestival;
        private String discountinfofestival;
        private String spendtimefestival;
        private String festivalgrade;
        private String progresstype;
        private String festivaltype;
    }
}
