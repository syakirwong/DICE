package com.alliance.diceintegration.request;

import lombok.Data;

@Data
public class ProfileInfoRequest {
    private String schema;
    private String table;
    private String detailID;
}
