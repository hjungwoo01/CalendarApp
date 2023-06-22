package com.hjungwoo01.calendarapp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Event implements Serializable {
    private long id;
    private String eventName;
    private String eventMemo;
    private String eventStart; //yyyyMMddHHmm
    private String eventEnd; //yyyyMMddHHmm
    private String eventRepeat;
    private String eventEndRepeat; //yyyyMMdd
    private final String[] intervalOptions = {"Never", "Every Day", "Every Week", "Every Month", "Every Year"};
    public static List<Event> eventsList = new ArrayList<>();

    public static ArrayList<Event> eventsForDate(LocalDate selectedDate) {
        ArrayList<Event> events = new ArrayList<>();
        for(Event e : eventsList) {
            if(e.getStartDate().equals(selectedDate) || (selectedDate.isAfter(e.getStartDate()) &&
                    selectedDate.isBefore(e.getEndDate())) || selectedDate.isEqual(e.getEndDate())) {
                events.add(e);
            }
        }
        return events;
    }

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public String getEventName() { return this.eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public void setEventMemo(String eventMemo) { this.eventMemo = eventMemo; }

    public String getEventStart() { return this.eventStart; }
    public void setEventStart(String eventStart) { this.eventStart = eventStart; }

    public String getEventEnd() { return this.eventEnd; }
    public void setEventEnd(String eventEnd) { this.eventEnd = eventEnd; }
    public void setEventRepeat(String eventRepeat) { this.eventRepeat = eventRepeat; }

    public void setEventEndRepeat(String eventEndRepeat) { this.eventEndRepeat = eventEndRepeat; }
    public String getEventEndRepeat() { return this.eventEndRepeat; }

    public LocalDate getStartDate() throws DateTimeParseException {
        return parseDateTime(eventStart);
    }

    public LocalDate getEndDate() throws DateTimeParseException {
        return parseDateTime(eventEnd);
    }

    public LocalDate getRepeatEndDate() throws DateTimeParseException {
        return parseDate(eventEndRepeat);
    }

    private LocalDate parseDate(String date) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault());
        return LocalDate.parse(date, formatter);
    }

    private LocalDate parseDateTime(String dateTime) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm", Locale.getDefault());
        return LocalDate.parse(dateTime, formatter);
    }

    public int getStartYear() {
        return Integer.parseInt(this.eventStart.substring(0,4));
    }

    public int getStartMonth() {
        return Integer.parseInt(this.eventStart.substring(4,6));
    }

    public int getStartDay() {
        return Integer.parseInt(this.eventStart.substring(6,8));
    }

    public int getStartHour() {
        return Integer.parseInt(this.eventStart.substring(8,10));
    }

    public int getStartMinute() {
        return Integer.parseInt(this.eventStart.substring(10,12));
    }

    public int getEndYear() {
        return Integer.parseInt(this.eventEnd.substring(0,4));
    }

    public int getEndMonth() {
        return Integer.parseInt(this.eventEnd.substring(4,6));
    }

    public int getEndDay() {
        return Integer.parseInt(this.eventEnd.substring(6,8));
    }

    public int getEndHour() {
        return Integer.parseInt(this.eventEnd.substring(8,10));
    }

    public int getEndMinute() {
        return Integer.parseInt(this.eventEnd.substring(10,12));
    }

    public int getRepeatEndYear() {
        return Integer.parseInt(this.eventEndRepeat.substring(0,4));
    }
    public int getRepeatEndMonth() {
        return Integer.parseInt(this.eventEndRepeat.substring(4,6));
    }
    public int getRepeatEndDay() {
        return Integer.parseInt(this.eventEndRepeat.substring(6,8));
    }

    public String getEventMemo() {
        return this.eventMemo;
    }

    public String getEventRepeat() {
        return this.eventRepeat;
    }

    public boolean isAllDay() {
        return getStartDay() == getEndDay() && getStartMonth() == getEndMonth() && getStartYear() == getEndYear()
                && getStartHour() == 0 && getStartMinute() == 0 && getEndHour() == 23 && getEndMinute() == 59;
    }

    public int getRepeatPosition() {
        for(int i = 0; i < intervalOptions.length; i++) {
            if(intervalOptions[i].equals(this.eventRepeat)) {
                return i;
            }
        }
        return 0;
    }

}