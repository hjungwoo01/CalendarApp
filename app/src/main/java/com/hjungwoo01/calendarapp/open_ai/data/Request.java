package com.hjungwoo01.calendarapp.open_ai.data;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {
    String model;
    List<Message> messages;
    float temperature;
    int max_tokens;
    int top_p;
    float frequency_penalty;
    float presence_penalty;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }

    public int getTop_p() {
        return top_p;
    }

    public void setTop_p(int top_p) {
        this.top_p = top_p;
    }

    public float getFrequency_penalty() {
        return frequency_penalty;
    }

    public void setFrequency_penalty(float frequency_penalty) {
        this.frequency_penalty = frequency_penalty;
    }

    public float getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(float presence_penalty) {
        this.presence_penalty = presence_penalty;
    }
}
