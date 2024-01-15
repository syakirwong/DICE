package com.alliance.dicerecommendation.request;

import lombok.Data;

@Data
public class DeleteRecommendationRequest {
    private Integer campaignId;
    private String cifNo;
}
