package com.alliance.dicerecommendation.model;

import java.util.Set;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_MEDIA_ASSET")
public class MediaAsset extends BaseInfo {
    @PrimaryKey(value = "MEDIA_ASSET_ID")
    private int mediaAssetId;

    @Column(value = "MEDIA_ASSET_NAME")
    private String mediaAssetName;

    @Column(value = "MEDIA_ASSET_TYPE")
    private String mediaAssetType;
    
    @Column(value = "MEDIA_ASSET_PATH_URL")
    private String mediaAssetPathUrl;
    
    @Column(value = "MEDIA_ASSET_DESCRIPTION")
    private String mediaAssetdescription;
}