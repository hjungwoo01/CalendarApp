package com.hjungwoo01.calendarapp.open_ai;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GSON {

    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String jsonString, Class<T> classOfT) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}
