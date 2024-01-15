package com.alliance.diceintegration.request;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class UserDetails {
    private String user_id;
    @Nullable
    private String user_name;
    @Nullable
    private Boolean overwrite_user_data;
    private Object user_data;
}
