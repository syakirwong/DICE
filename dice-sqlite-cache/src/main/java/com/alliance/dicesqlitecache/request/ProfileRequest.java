package com.alliance.dicesqlitecache.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
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
