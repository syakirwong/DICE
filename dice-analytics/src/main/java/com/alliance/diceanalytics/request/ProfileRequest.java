package com.alliance.diceanalytics.request;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {

    @JsonAlias("idType")
    private String idType;

    @JsonAlias("idValue")
    private String idValue;

    @JsonAlias("tableName")
    private String tableName;
}
