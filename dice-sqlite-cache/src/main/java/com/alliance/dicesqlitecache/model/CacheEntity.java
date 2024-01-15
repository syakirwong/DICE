package com.alliance.dicesqlitecache.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "CACHE_ENTITY")
public class CacheEntity {
    @Id
    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "source_table")
    private String sourceTable;

    @Column(name = "source_database")
    private String sourceDatabase;
}
