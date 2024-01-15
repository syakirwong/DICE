package com.alliance.diceintegration.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BroadcastData {
    private List<UserDetails> user_details = new ArrayList<UserDetails>();
    private List<Messages> messages= new ArrayList<Messages>();
    private String platform;
}