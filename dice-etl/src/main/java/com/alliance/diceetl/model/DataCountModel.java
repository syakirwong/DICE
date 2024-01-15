package com.alliance.diceetl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCountModel {
    private Long readCount;
    private Long skipCount;
    private Long writeCount;


    public DataCountModel(AtomicLong readCount, AtomicLong skipCount, AtomicLong writeCount) {
        this.readCount = readCount.get();
        this.skipCount = skipCount.get();
        this.writeCount = writeCount.get();
    }
}
