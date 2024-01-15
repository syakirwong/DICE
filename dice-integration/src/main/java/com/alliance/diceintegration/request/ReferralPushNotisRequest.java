package com.alliance.diceintegration.request;

import java.util.UUID;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Data;

@Data
public class ReferralPushNotisRequest {
    @Nullable
    private String title;
    @Nullable
    private String message;
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
    private UUID contentId;
}
