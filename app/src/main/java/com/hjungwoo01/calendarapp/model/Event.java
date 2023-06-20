package com.hjungwoo01.calendarapp.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event implements Serializable {
    private long id;
    private String eventName;
    private String eventMemo;
    private String eventStart; //yyyyMMddHHmm
    private String eventEnd; //yyyyMMddHHmm
    private String eventRepeat;

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

    public Date getStartDate() throws ParseException {
        return parseDateTime(eventStart);
    }

    public Date getEndDate() throws ParseException {
        return parseDateTime(eventEnd);
    }

    private Date parseDateTime(String dateTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
        return format.parse(dateTime);
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

    public String getEventMemo() {
        return this.eventMemo;
    }

    public String getEventRepeat() {
        return this.eventRepeat;
    }

    public boolean isAllDay() {
        if(getStartDay() == getEndDay() && getStartMonth() == getEndMonth() && getStartYear() == getEndYear()
         && getStartHour() == 0 && getStartMinute() == 0 && getEndHour() == 23 && getEndMinute() == 59) {
            return true;
        }
        return false;
    }
}