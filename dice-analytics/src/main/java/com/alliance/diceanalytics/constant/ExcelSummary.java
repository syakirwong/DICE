package com.alliance.diceanalytics.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ExcelSummary {
    DATE_RANGE("Date Range");

    private String itemName;

    ExcelSummary(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public static Map<String, String> getNameMappings() {
        Map<String, String> nameMap = new HashMap<>();
        Arrays.stream(ExcelSummary.values()).forEach(as -> {
            nameMap.put(as.name(), as.getItemName());
        });
        return nameMap;
    }
}
