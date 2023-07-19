package com.hjungwoo01.calendarapp.open_ai;

import org.json.JSONException;

public interface RetrofitCallback<T> {

    void onSuccess(int code, T receivedData) throws JSONException;

    void onFailure(int code);

    void onError(Throwable t);
}
