package com.alliance.dicerecommendation.response;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class PloanAssetsResponse {
    @Nullable
    private String promoImg;
    @Nullable
    private String promoDesc;
    @Nullable
    private String promoBtnType;
    @Nullable
    private String promoBtnDesc;
    @Nullable
    private String promoBackgroundStyle;
    @Nullable
    private String submitBtnType;
    @Nullable
    private Boolean isShowPromo;
    @Nullable
    private String okBtnType;
}
