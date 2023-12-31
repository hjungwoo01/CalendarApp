package com.hjungwoo01.calendarapp.open_ai.data;

import java.io.Serializable;

public class Message implements Serializable {
    String role;
    String content;
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
