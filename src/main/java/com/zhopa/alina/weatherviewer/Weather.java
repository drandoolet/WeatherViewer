package com.zhopa.alina.weatherviewer;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Weather {
    public final String dayOfWeek, minTemp, maxTemp, humidity, description, iconURL;

    Weather(long timeStamp, double minTemp, double maxTemp, double humidity, String description, String iconName) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        dayOfWeek = convertTimeStampToDay(timeStamp);
        this.minTemp = numberFormat.format(minTemp) + "\u00B0C";
        this.maxTemp = numberFormat.format(maxTemp) + "\u00B0C";
        this.humidity = NumberFormat.getPercentInstance().format(humidity / 100.0);
        this.description = description;
        iconURL = iconName;
    }

    public static String convertTimeStampToDay(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000);
        TimeZone tz = TimeZone.getDefault();

        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        //SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();

        return dateFormat.format(calendar.getTime());
    }
}
