package com.alliance.diceintegration.model;

import java.util.Set;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_MESSAGE_TEMPLATE")
public class MessageTemplate extends BaseInfo {
    @PrimaryKey
    private Integer messageTemplateId;
    private String description;
    private String title;
    private String language;
    private String content;
    private Set<String> buttonIds;
    private String messageTemplateName;
    private String communicationChannel;
}