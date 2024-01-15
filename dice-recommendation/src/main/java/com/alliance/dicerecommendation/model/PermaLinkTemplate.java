package com.alliance.dicerecommendation.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_PERMA_LINK_TEMPLATE")
public class PermaLinkTemplate extends BaseInfo{
    @PrimaryKey
    private Integer permaLinkTemplateId;
    private String type;
    private String title;
    private Integer iconImageId;
}
