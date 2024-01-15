package com.alliance.dicerecommendation.response;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class PermenantLinkResponse {
    @Nullable
    private String type;
    @Nullable
    private String backgroundStyle;
    @Nullable
    private String iconImg;
    @Nullable
    private String titleText;
    @Nullable
    private String titleStyle;
    @Nullable
    private String descriptionText;
    @Nullable
    private String descriptionStyle;
}
