package com.alliance.dicerecommendation.model;

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
}
