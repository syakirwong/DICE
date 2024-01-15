package com.alliance.dicerecommendation.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PandaiBroadcastUserResponse {
    private String user_id;
    private String created_at;
    private PandaiBroadcastUserProfileResponse user_profile;
}
