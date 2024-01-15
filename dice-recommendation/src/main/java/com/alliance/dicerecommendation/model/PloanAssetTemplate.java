package com.alliance.dicerecommendation.model;

import javax.annotation.Nullable;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Data
@Table("EVT_PLOAN_ASSET_TEMPLATE")
public class PloanAssetTemplate extends BaseInfo {
    @PrimaryKey
    private Integer ploanAssetTemplateId;
    private Integer promoImg;
    private String promoDesc;
    private String promoBtnType;
    private String promoBtnDesc;
    private String promoBackgroundStyle;
    private String submitBtnType;
    private Boolean isShowPromo;
    private String okBtnType;
}
