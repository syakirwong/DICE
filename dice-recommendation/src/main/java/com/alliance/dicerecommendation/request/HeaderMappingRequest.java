package com.alliance.dicerecommendation.request;

import java.util.List;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class HeaderMappingRequest {
    private String headerName;
    @Nullable
    private String description;
    @Nullable
    private String remark;
    private String headerType;
    private List<String> headerNameMapping;

    // Default constructor
    public HeaderMappingRequest() {
    }

    // Constructor with selected fields
    public HeaderMappingRequest(String headerName, List<String> headerNameMapping, String headerType) {
        this.headerName = headerName;
        this.headerType = headerType;
        this.headerNameMapping = headerNameMapping;
    }

    // Original constructor with all fields
    public HeaderMappingRequest(String headerName, String description, String remark, String headerType, List<String> headerNameMapping) {
        this.headerName = headerName;
        this.description = description;
        this.remark = remark;
        this.headerType = headerType;
        this.headerNameMapping = headerNameMapping;
    }
}