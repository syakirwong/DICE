package com.alliance.dicesqlitecache.request;

import lombok.Data;

@Data
public class CacheRequest {
    private String value;
    private String sourceDatabase;
    private String sourceTable;
}
