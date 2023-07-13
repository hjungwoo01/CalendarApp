package com.hjungwoo01.calendarapp.open_ai;

public interface RetrofitCallback<T> {

    void onSuccess(int code, T receivedData);

    void onFailure(int code);

    void onError(Throwable t);
}
