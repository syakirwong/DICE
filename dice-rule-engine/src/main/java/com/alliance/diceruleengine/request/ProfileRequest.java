package com.alliance.diceruleengine.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ProfileRequest {
    @JsonAlias("idType")
    private String idType;

    @JsonAlias("idValue")
    private String idValue;

    @JsonAlias("tableName")
    private String tableName;
}
