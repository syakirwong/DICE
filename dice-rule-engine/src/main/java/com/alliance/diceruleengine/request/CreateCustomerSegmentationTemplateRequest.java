package com.alliance.diceruleengine.request;

import lombok.Data;

@Data
public class CreateCustomerSegmentationTemplateRequest {
    private String customerSegmentType;
    private String customerSegmentValue;
    private String customerSegmentBehaviour;
    private String description;
}
