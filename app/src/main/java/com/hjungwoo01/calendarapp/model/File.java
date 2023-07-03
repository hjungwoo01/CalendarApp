package com.hjungwoo01.calendarapp.model;

public class File {
    private long id;
    private long memoId;
    private String name;
    private String type;
    private byte[] data;

    public long getId() { return this.id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }
    public long getMemoId() {
        return memoId;
    }
    public void setMemoId(long id) {
        this.memoId = id;
    }
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
}
