package com.alliance.dicecontentmanagement.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_BUTTON_TEMPLATE")
public class ButtonTemplate extends BaseInfo {
    @PrimaryKey
    private Integer buttonTemplateId;
    private String templateStatus;
    private String content;
    private String buttonTemplateName;
    private String buttonType;
}
