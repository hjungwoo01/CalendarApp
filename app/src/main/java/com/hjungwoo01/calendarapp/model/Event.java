package com.hjungwoo01.calendarapp.model;

public class Event {
    private long id;
    private String eventName;
    private String eventMemo;
    private int eventStart;
    private int eventEnd;
    private int eventRepeat;

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public String getEventName() { return this.eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public void setEventMemo(String eventMemo) { this.eventMemo = eventMemo; }

    public int getEventStart() { return this.eventStart; }
    public void setEventStart(int eventStart) { this.eventStart = eventStart; }

    public int getEventEnd() { return this.eventEnd; }
    public void setEventEnd(int eventEnd) { this.eventEnd = eventEnd; }

    public void setEventRepeat(int eventRepeat) { this.eventRepeat = eventRepeat; }
}