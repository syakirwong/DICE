package com.alliance.diceintegration.utility;

import org.springframework.stereotype.Component;

@Component
public class MathUtil {
    public static Integer getHighestAvailableDigitForAlphaNumeric(Integer num) {

        if (num < ((Double) Math.pow(62, 6)).intValue()) {
            return 6;
        } else if (num < ((Double) Math.pow(62, 7)).intValue()) {
            return 7;
        } else if (num < ((Double) Math.pow(62, 8)).intValue()) {
            return 8;
        } else if (num < ((Double) Math.pow(62, 9)).intValue()) {
            return 9;
        } else if (num < ((Double) Math.pow(62, 10)).intValue()) {
            return 10;
        } else if (num < ((Double) Math.pow(62, 11)).intValue()) {
            return 11;
        } else if (num < ((Double) Math.pow(62, 12)).intValue()) {
            return 12;
        } else if (num < ((Double) Math.pow(62, 13)).intValue()) {
            return 13;
        } else if (num < ((Double) Math.pow(62, 14)).intValue()) {
            return 14;
        } else if (num < ((Double) Math.pow(62, 15)).intValue()) {
            return 15;
        } else if (num < ((Double) Math.pow(62, 16)).intValue()) {
            return 16;
        } else if (num < ((Double) Math.pow(62, 17)).intValue()) {
            return 17;
        } else if (num < ((Double) Math.pow(62, 18)).intValue()) {
            return 18;
        } else if (num < ((Double) Math.pow(62, 19)).intValue()) {
            return 19;
        } else if (num < ((Double) Math.pow(62, 20)).intValue()) {
            return 20;
        }
        return 0;
    }

}
