package com.hjungwoo01.calendarapp.model;

public class Memo {
    private long id;
    private String owner;
    private String receiver;
    private String memoName;
    private String memo;
    private String date;
    private String readReceivers;

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getOwner() { return this.owner; }

    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getReceiver() { return this.receiver; }
    public void setMemoName(String memoName) { this.memoName = memoName; }
    public String getMemoName() { return this.memoName; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getMemo() { return this.memo; }
    public void setDate(String date) { this.date = date; }
    public String getDate() { return this.date; }

    public int getCreatedYear() {
        return Integer.parseInt(this.date.substring(0,4));
    }

    public int getCreatedMonth() {
        return Integer.parseInt(this.date.substring(4,6));
    }

    public int getCreatedDay() {
        return Integer.parseInt(this.date.substring(6,8));
    }

    public int getCreatedHour() {
        return Integer.parseInt(this.date.substring(8,10));
    }

    public int getCreatedMinute() {
        return Integer.parseInt(this.date.substring(10,12));
    }

    public String getReadReceivers() {
        return this.readReceivers;
    }

    public void setReadReceivers(String readReceivers) {
        this.readReceivers = readReceivers;
    }
}
