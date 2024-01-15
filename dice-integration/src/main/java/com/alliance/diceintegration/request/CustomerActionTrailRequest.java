package com.alliance.diceintegration.request;

import com.alliance.diceintegration.constant.DataField.ActionStatus;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.Set;

@Data
public class CustomerActionTrailRequest {
    @Nullable
    private String event;
    @Nullable
    private String action;
    @Nullable
    private String actionPage;
    @Nullable
    private String channel;
    @Nullable
    private Set<String> engagementMode;
    @Nullable
    private String cifNo;
    @Nullable
    private String deviceId;
    @Nullable
    private String devicePlatform;
    @Nullable
    private Boolean isLogin;
    @Nullable
    private ActionStatus actionStatus;
    @Nullable
    private Integer campaignId;
    @Nullable
    private String sessionId;
}
