package com.hjungwoo01.calendarapp.open_ai.data;

import java.io.Serializable;

public class Choices implements Serializable {
    Message message;
    String finish_reason;
    int index;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
