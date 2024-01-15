package com.alliance.diceruleengine.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class RuleUtil {

    public static Integer calculateAge(String dateOfBirth) {

        try {
            // Define the format of the input date string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");

            // Parse the input date string into a LocalDate object
            LocalDate dob = LocalDate.parse(dateOfBirth, formatter);

            // Calculate the difference between the birth date and the current date
            long years = ChronoUnit.YEARS.between(dob, LocalDate.now());

            // Return the age as an Integer
            return Math.toIntExact(years);
        } catch (DateTimeParseException | ArithmeticException | NullPointerException e) {
            // Handle any exceptions that may occur during parsing or calculation
            e.printStackTrace();
            return null; // or throw an exception or return a default value
        }
    }

}
