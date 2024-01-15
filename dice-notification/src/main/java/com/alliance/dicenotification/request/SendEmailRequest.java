package com.alliance.dicenotification.request;

import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.experimental.Tolerate;

@Data
public class SendEmailRequest extends BaseRequest{
   
    private String title;
    private String content;
    @Nullable
    private String deviceId;
    @Nullable
    private String devicePlatform;
    @Nullable
    private String notificationType;
    private String eformUuid;
    private String engagementId;
    private String to;
    private String from;
    private String cc;
    private Integer engagementSentId;

    public SendEmailRequest(String title,String content,String to,String from,String cc){

        this.title=title;
        this.content=content;
        this.to=to;
        this.from=from;
        this.cc=cc;
    }

    // @Nullable
    // private String userRole;
}
