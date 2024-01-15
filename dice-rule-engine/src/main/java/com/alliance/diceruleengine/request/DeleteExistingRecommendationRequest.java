package com.alliance.diceruleengine.request;

import lombok.Data;

@Data
public class DeleteExistingRecommendationRequest {
    private Integer campaignId;
    private String cifNo;
}
