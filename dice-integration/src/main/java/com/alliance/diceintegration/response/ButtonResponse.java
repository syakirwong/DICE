package com.alliance.diceintegration.response;

import lombok.Data;

@Data
public class ButtonResponse {
    private String btnType;
    private String btnName;
    private String content;

    public ButtonResponse() {

    }

}
