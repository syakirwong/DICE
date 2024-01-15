package com.alliance.diceintegration.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.Data;

@Data
@Table("EVT_BUTTON_LIST")
public class ButtonList extends BaseInfo {
    @PrimaryKey(value = "BUTTON_ID")
    private Integer buttonId;

    @Column(value = "BUTTON_TEMPLATE_ID")
    private Integer buttonTemplateId;

    @Column(value = "BUTTON_STYLE_TEMPLATE_ID")
    private Integer buttonStyleTemplateId;

    @Column(value = "BUTTON_IN_APP_CONTENT_TEMPLATE_ID")
    private Integer buttonInAppContentTemplateId;
}