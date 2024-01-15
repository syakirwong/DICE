package com.alliance.dicerecommendation.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PandaiBroadcastResponse implements Serializable {
    private String platform;

    @JsonUnwrapped
    private List<PandaiBroadcastUserResponse> successful_users;

    @JsonUnwrapped
    private List<PandaiBroadcastUserResponse> failed_users;
}
