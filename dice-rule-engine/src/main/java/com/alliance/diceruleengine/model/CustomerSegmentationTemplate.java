package com.alliance.diceruleengine.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.alliance.diceruleengine.constant.DataField.Status;

import lombok.Data;

@Data
@Table("EVT_CUSTOMER_SEGMENTATION_TEMPLATE")
public class CustomerSegmentationTemplate extends BaseInfo {
    @PrimaryKey
    private Integer customerSegmentationTemplateId;
    private Status templateStatus;
    private String customerSegmentType;
    private String customerSegmentValue;
    private String customerSegmentBehaviour;
    private String description;
}
