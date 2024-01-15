package com.alliance.dicecampaign.response;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract class BaseResponse {

    public static final String DF_DATE_ONLY = "dd/MM/yyyy";
    public static final String DF_TIME_ONLY = "HH:mm:ss";
    public static final String DF_DATE_TIME = "dd/MM/yyyy HH:mm:ss";

    public String convertDateToString(String format, Date date) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String str = sdf.format(date);

        return str;
    }

    public String convertDateAndTimeToString(Date date, Date time) {
        if (date == null || time == null) {
            return null;
        }

        SimpleDateFormat dateSdf = new SimpleDateFormat(DF_DATE_ONLY);
        SimpleDateFormat timeSdf = new SimpleDateFormat(DF_TIME_ONLY);
        String dateString = dateSdf.format(date);
        String timeString = timeSdf.format(time);

        return String.format("%s %s", dateString, timeString);
    }

    public Date parseToDateFormat(String date) {
        try {
            return new SimpleDateFormat(DF_DATE_ONLY).parse(date);
        } catch (ParseException ex) {
        }
        return null;
    }

    public String convertAmountToCurrencyString(BigDecimal amount) {
        return String.format("RM %,.2f", amount);
    }

}
