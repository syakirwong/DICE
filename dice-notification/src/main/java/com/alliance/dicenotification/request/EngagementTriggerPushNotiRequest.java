package com.alliance.dicenotification.request;

import org.springframework.lang.Nullable;

import lombok.Data;

@Data
public class EngagementTriggerPushNotiRequest extends BaseRequest {
    @Nullable
    private String title;
    @Nullable
    private String content;
    @Nullable
    private String deviceId;
    @Nullable
    private String devicePlatform;
    @Nullable
    private String notificationType;
    @Nullable
    private String applicationSessionId;
    @Nullable
    private String engagementId;
    @Nullable
    private Integer contentId;
    @Nullable
    private String messageDisplayMethod;
    @Nullable
    private String type;

}



