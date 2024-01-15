package com.alliance.dicecontentmanagement.request;

import java.util.Set;

import com.alliance.dicecontentmanagement.model.ButtonTemplate;

import lombok.Data;

@Data
public class CreateMessageTemplateRequest extends BaseRequest {
    private String description;
    private String title;
    private String language;
    private String content;
    private Set<String> buttonIds;
    private String messageTemplateName;
    private String communicationChannel;
}
