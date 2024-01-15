package com.alliance.dicerecommendation.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class CustomerProfile {
    private String uuid;
    private String idNo;
    private String fullName;
    private String promoCode;
    private String mobile;
    private String deviceUuid;
    private String devicePlatform;
    private String completedOn;
    private String statusCode;
    private String pdpaFlag;
    // private String custAccNo;

    public void setCompletedOn(Date completedOn) {
        this.completedOn = convertDateToString("dd/MM/yyyy hh:mm a", completedOn);
    }

	public String convertDateToString(String format, Date date) {
		if (date == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String str = sdf.format(date);
		
		return str;
	}
}
