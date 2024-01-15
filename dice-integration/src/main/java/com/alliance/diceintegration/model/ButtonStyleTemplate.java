package com.alliance.diceintegration.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_BUTTON_STYLE_TEMPLATE")
public class ButtonStyleTemplate extends BaseInfo {
    @PrimaryKey
    private Integer buttonStyleTemplateId;
    private String templateStatus;
    private String style;
    private String buttonStyleTemplateName;

}

