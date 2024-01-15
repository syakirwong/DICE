package com.alliance.dicerecommendation.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PandaiBroadcastRequest extends BaseRequest {
    private List<BroadcastData> broadcast_data = new ArrayList<BroadcastData>();
    private String abbr;
}
