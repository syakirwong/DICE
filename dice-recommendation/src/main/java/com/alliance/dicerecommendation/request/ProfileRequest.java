package com.alliance.dicerecommendation.request;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ProfileRequest {

    @NotBlank(message = "IdType is required.")
    @JsonAlias("idType")
    private String idType;

    @NotBlank(message = "IdValue is required.")
    @JsonAlias("idValue")
    private String idValue;

    @NotBlank(message = "TableName is required.")
    @JsonAlias("tableName")
    private String tableName;
}
