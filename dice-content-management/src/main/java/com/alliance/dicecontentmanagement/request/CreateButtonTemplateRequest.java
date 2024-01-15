package com.alliance.dicecontentmanagement.request;

import lombok.Data;

@Data
public class CreateButtonTemplateRequest extends BaseRequest {
    private String status;
    private String content;
    private String buttonTemplateName;
    private String buttonType;
}
