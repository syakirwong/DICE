package com.alliance.dicecontentmanagement.request;

import lombok.Data;

@Data
public class CreateButtonStyleTemplateRequest extends BaseRequest {
    private String status;
    private String style;
    private String buttonStyleTemplateName;
}
