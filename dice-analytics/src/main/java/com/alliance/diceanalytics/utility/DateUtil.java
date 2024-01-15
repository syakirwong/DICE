package com.alliance.diceanalytics.utility;

import com.alliance.diceanalytics.request.BaseReportInfoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
public class DateUtil {
    private static String DF_12_HOURS = "hh:mm a";
    private static String DF_24_HOURS = "HH:mm:ss";
    private static String DF_DATE = "dd/MM/yyyy";
    private static String DF_DATETIME = "dd/MM/yyyy hh:mm a";

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

    public static List<Date> getCurrentAndLastWeekDateRange() {
        List<Date> dateRange = new ArrayList<>();
    
        // Get current date and time
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        dateRange.add(currentDate);
    
        // Set to last Monday 00:00:00
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.DATE, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date responseStartDate = cal.getTime();
        dateRange.add(responseStartDate);
    
        // Set to Sunday 23:59:59
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date responseEndDate = cal.getTime();
        dateRange.add(responseEndDate);
    
        return dateRange;
    }

    public static List<Date> getLastYearUntilTodayDateRange() {
        List<Date> dateRange = new ArrayList<>();
    
        // Get current date and time
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        dateRange.add(currentDate);
    
        // Set to last year's January 1st 00:00:00
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date responseStartDate = cal.getTime();
        dateRange.add(responseStartDate);
    
        // Set to today at 23:59:59
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date responseEndDate = cal.getTime();
        dateRange.add(responseEndDate);
    
        return dateRange;
    }

    public static String formatDateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(date);
    }

    public static BaseReportInfoRequest adjustDateTime (BaseReportInfoRequest request){
        if (request.getStartDate()!=null)
        request.setStartDate(Date.from(Instant.ofEpochMilli(request.getStartDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .atZone(ZoneId.ofOffset("UTC", ZoneOffset.of("+00")))
                .toInstant()));

        if (request.getEndDate()!=null)
        request.setEndDate(Date.from(Instant.ofEpochMilli(request.getEndDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .atZone(ZoneId.ofOffset("UTC", ZoneOffset.of("+00")))
                .toInstant()));

        return request;
    }

    public static BaseReportInfoRequest getReportDateRange(String duration){
        BaseReportInfoRequest request = new BaseReportInfoRequest();

        LocalDate initialEndDate = LocalDate.now().minusDays(1);
        //12:00:00 am
        Date endDateMorning = Date.from(initialEndDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        //Yesterday / Report End Date 23:59:59 pm
        Date endDate = Date.from(initialEndDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        //Weekly start date  12:00:00 am
        Date startDate =  Date.from(initialEndDate.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //Monthly start date 12:00:00 am
        Date monthDateStart =  Date.from(LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        //23:59:59 pm
        Date monthDateEnd =  Date.from(LocalDate.now().minusDays(1).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        switch (duration){
            case "daily":
                request =new BaseReportInfoRequest(endDateMorning, endDate, "", "");
                break;
            case "monthly":
                request =new BaseReportInfoRequest(monthDateStart, monthDateEnd, "", "");
                break;
            case "weekly":
                request =new BaseReportInfoRequest(startDate, endDate, "", "");
                break;
            default:
                break;
        }

        return request;

    }
    
    
}
