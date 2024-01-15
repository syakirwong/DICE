package com.alliance.diceanalytics.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)

public class CustomerProfileEFormResponse {
    @JsonAlias({"uuid"})
    private String uuid;

    @JsonAlias("id_no")
    private String idNo;

    @JsonAlias({"full_name"})
    private String fullName;

    @JsonAlias({"promo_code"})
    private String promoCode;

    @JsonAlias({"mobile"})
    private String mobile;

    @JsonAlias({"device_uuid"})
    private String deviceUuid;

    @JsonAlias({"device_platform"})
    private String devicePlatform;

    @JsonAlias("completed_on")
    private Date completedOn;

    @JsonAlias("status_code")
    private String statusCode;

    @JsonAlias("pdpa_flag")
    private String pdpaFlag;
}
