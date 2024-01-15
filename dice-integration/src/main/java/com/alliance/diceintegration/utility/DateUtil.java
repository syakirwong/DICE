package com.alliance.diceintegration.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DateUtil {
    public static String DF_12_HOURS = "hh:mm a";
    public static String DF_24_HOURS = "HH:mm:ss";
    public static String DF_DATE = "dd/MM/yyyy";
    public static String DF_DATETIME = "dd/MM/yyyy hh:mm a";
    public static String DF_DB2DATETIME = "yyyy-MM-dd-HH.mm.ss.SSSSSS";

    public static Date getDatetimeByDateAndTime(Date date, Date time) {
        Date utilDate = new Date(date.getTime());
        Date utilTime = new Date(time.getTime());
        LocalDate localDate = utilDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalTime localTime = utilTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        return Date.from(LocalDateTime.of(localDate, localTime)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static Date parseFrom12HoursFormat(String time) throws ParseException {
        return new SimpleDateFormat(DF_12_HOURS).parse(time);
    }

    public static Date parseFrom24HoursFormat(String time) throws ParseException {
        return new SimpleDateFormat(DF_24_HOURS).parse(time);
    }

    public static String formatTo12HoursFormat(Date time) {
        return new SimpleDateFormat(DF_12_HOURS).format(time);
    }

    public static String formatTo24HoursFormat(Date time) {
        return new SimpleDateFormat(DF_24_HOURS).format(time);
    }

    public static String convert12HoursFormatTo24HoursFormat(String timeIn12HoursFormat) throws ParseException {
        return formatTo24HoursFormat(parseFrom12HoursFormat(timeIn12HoursFormat));
    }

    public static String formatToDateFormat(Date date) {
        if(date == null) {
            return null;
        }
        return new SimpleDateFormat(DF_DATE).format(date);
    }

    public static String formatToDateFormat(Date date, String dateFormat) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(dateFormat).format(date);
    }

    public static String formatToDateTimeFormat(Date date) {
        return new SimpleDateFormat(DF_DATETIME).format(date);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Date utilDate1 = new Date(date1.getTime());
        Date utilDate2 = new Date(date2.getTime());
        LocalDate localDate1 = utilDate1.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate localDate2 = utilDate2.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate1.isEqual(localDate2);
    }

    public static Date convertToDate(String format, String dateStr) {
		Date date = null;
		if (dateStr != null && !dateStr.isEmpty()) {
			try {
				date = new SimpleDateFormat(format).parse(dateStr);
			} catch (ParseException ex) {
				log.error("convertToDate", ex);
			}
		}

		return date;
	}

    public static Integer getDaysOfTheYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    public static Integer getDayDifference(Date startDate, Date endDate) {
        return (int) ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());
    }

    public static Integer getHourDifference(Date startDate, Date endDate) {
        log.info(String.format("%d",ChronoUnit.HOURS.between(startDate.toInstant(), endDate.toInstant())));
        return (int) ChronoUnit.HOURS.between(startDate.toInstant(), endDate.toInstant());
    }

    public static Integer getMillisDifference(Date startDate, Date endDate) {
        log.info(String.format("%d",ChronoUnit.MILLIS.between(startDate.toInstant(), endDate.toInstant())));
        return (int) ChronoUnit.MILLIS.between(startDate.toInstant(), endDate.toInstant());
    }

    public static Integer getMonthDifference(Date startDate, Date endDate) {
        Period period = Period.between(
                startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
        );
        return (int) period.toTotalMonths();
    }

    public static Date getMinuteAfter(Date date, Integer minutesAfter) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutesAfter);
        return cal.getTime();
    }

    public static Date getHourAfter(Date date, Integer hoursAfter) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hoursAfter);
        return cal.getTime();
    }

    public static Date getDayAfter(Date date, Integer daysAfter) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, daysAfter);
        return cal.getTime();
    }

    public static Date getMonthAfter(Date date, Integer monthsAfter) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, monthsAfter);
        return cal.getTime();
    }

    public static Date getFirstDayOfMonth(Date date, Integer numberOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, numberOfMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date getFirstDayOfMonthAndBeginningOfDate(Date date, Integer month) {
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
        cal.add(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    date = cal.getTime();

	    return date;
	}

	public static Date getLastDayOfMonthAndEndOfDate(Date date, Integer month) {
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
        cal.add(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
	    cal.set(Calendar.HOUR_OF_DAY, 23);
	    cal.set(Calendar.MINUTE, 59);
	    cal.set(Calendar.SECOND, 59);
	    date = cal.getTime();

	    return date;
	}

    public static Date setTimeToZero(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date setTimeToMax(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(setTimeToZero(date));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        return cal.getTime();
    }

    public static String getOrdinal(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String ordinal;
        switch (day % 20) {
            case 1:
                ordinal = "st";
                break;
            case 2:
                ordinal = "nd";
                break;
            case 3:
                ordinal = "rd";
                break;
            default:
                ordinal = day > 30 ? "st" : "th";
        }
        return ordinal;
    }

    public static Date getPreviousYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, -1);
        return cal.getTime();
    }

    public static long getDateInMs(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        if (cal.get(Calendar.MILLISECOND) >= 500 ) {
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 1);
        }else {
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTimeInMillis();
    }
    
    public static List<Date> getDayRangeForQuery(Date startDate, Date endDate) {
    	
    	List<Date> dateRange = new ArrayList<>();
    	
    	Calendar cal = Calendar.getInstance();
    	
    	cal.setTime(startDate);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	
    	dateRange.add(cal.getTime());
    	
    	cal.setTime(endDate);
    	cal.set(Calendar.HOUR_OF_DAY, 23);
    	cal.set(Calendar.MINUTE, 59);
    	cal.set(Calendar.SECOND, 59);
    	
    	dateRange.add(cal.getTime());
    	
    	return dateRange;
    }
    
    public static Date addEpochSeconds (Date date, long milliSeconds) {
    	
    	long modifiedTime = date.toInstant().toEpochMilli() + milliSeconds;
    	
    	return Date.from(Instant.ofEpochMilli(modifiedTime));
    	
    }
    
    public static Date minusEpochSeconds (Date date, long milliSeconds) {
    	
    	long modifiedTime = date.toInstant().toEpochMilli() - milliSeconds;
    	
    	return Date.from(Instant.ofEpochMilli(modifiedTime));
    	
    }
}

