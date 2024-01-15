package com.alliance.diceintegration.response;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import lombok.Data;

@Data
public class MessageContentResponse {
    private String content;
    @Nullable
    private String title;
    private List<ButtonResponse> buttons = new ArrayList<ButtonResponse>();
}
