package com.alliance.dicerecommendation.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PandaiBroadcastUserProfileResponse {
    private String name;
    private String referral_code;
    private String referee_code;
}
