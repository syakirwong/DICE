package com.alliance.dicecontentmanagement.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.Column;

import com.alliance.dicecontentmanagement.constant.DataField.Status;
import com.fasterxml.jackson.annotation.JsonFormat;


@MappedSuperclass
@Data
abstract class BaseInfo implements Serializable {

    @Column(value = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(value = "CREATED_ON")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	private Date createdOn = new Date();

    @Column(value = "CREATED_BY")
    private String createdBy = "SYSTEM";

    @Column(value = "UPDATED_ON")
    @LastModifiedDate
    private Date updatedOn;

    @Column(value = "UPDATED_BY")
    private String updatedBy = "SYSTEM";
}
