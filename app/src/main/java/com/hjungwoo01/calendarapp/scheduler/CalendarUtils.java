package com.hjungwoo01.calendarapp.scheduler;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarUtils {
    public static LocalDate selectedDate;
    private static Map<YearMonth, ArrayList<LocalDate>> monthCache = new HashMap<>();

    public static String formattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        return date.format(formatter);
    }

    public static String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public static ArrayList<LocalDate> daysInMonthArray(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);

        if (monthCache.containsKey(yearMonth)) {
            return monthCache.get(yearMonth);
        }

        ArrayList<LocalDate> daysInMonthArray = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add(null);
            } else {
                daysInMonthArray.add(LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), i - dayOfWeek));
            }
        }

        monthCache.put(yearMonth, daysInMonthArray);
        return daysInMonthArray;
    }
}
