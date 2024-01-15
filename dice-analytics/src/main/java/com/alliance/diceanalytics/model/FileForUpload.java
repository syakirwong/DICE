package com.alliance.diceanalytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileForUpload {
	private byte[] fileData ;
	private String fileName = "";
	private Boolean sentMft = true;

	public FileForUpload(byte[] fileData, String fileName) {
		this.fileData = fileData;
		this.fileName = fileName;
	}
}

