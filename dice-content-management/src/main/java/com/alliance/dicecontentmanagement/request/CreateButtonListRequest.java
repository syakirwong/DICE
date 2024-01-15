package com.alliance.dicecontentmanagement.request;

import lombok.Data;

@Data
public class CreateButtonListRequest {
    private Integer buttonTemplateId;
    private Integer buttonStyleTemplateId;
    private Integer buttonInAppContentTemplateId;
}
