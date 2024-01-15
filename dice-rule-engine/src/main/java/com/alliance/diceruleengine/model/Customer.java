package com.alliance.diceruleengine.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Data;

@Data
@Entity
public class Customer {
    @Nullable
    private String customerCifNo;
    @Nullable
    private String customerName;
    @Nullable
    private String customerAddress;
    @Nullable
    private String customerCity;
    @Nullable
    private String customerState;
    @Nullable
    private String customerZip;
    @Nullable
    private String customerPhone;
    @Nullable
    private String customerDob;
    @Nullable
    private String customerType;
    @Nullable
    private String customerGender;
    @Nullable
    private Integer customerAge;
    @Nullable
    private Set<Integer> customerSegmentId = new HashSet<>();
    @Nullable
    private Set<Integer> campaignId = new HashSet<>();
    @Nullable
    private Set<String> campaignName = new HashSet<>();

    public boolean isEmpty() {
        return customerCifNo == null
                && customerName == null
                && customerAddress == null
                && customerCity == null
                && customerState == null
                && customerZip == null
                && customerPhone == null
                && customerDob == null
                && customerType == null
                && customerGender == null
                && customerAge == null
                && (customerSegmentId == null || customerSegmentId.isEmpty())
                && (campaignId == null || campaignId.isEmpty())
                && (campaignName == null || campaignName.isEmpty());
    }
}
