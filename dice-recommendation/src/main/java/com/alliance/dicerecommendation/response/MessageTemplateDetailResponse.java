package com.alliance.dicerecommendation.response;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class MessageTemplateDetailResponse {
    @Nullable
    private String title;
    @Nullable
    private String content;
}
