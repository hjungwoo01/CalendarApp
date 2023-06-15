package com.hjungwoo01.calendarapp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
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

    public String getEventMemo() { return this.eventMemo; }

    public String getEventRepeat() { return this.eventRepeat; }
}