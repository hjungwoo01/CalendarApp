package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface EventApi {
    @GET("/db_calendar/scheduler/get-all")
    Call<List<Event>> getAllEvents();

    @POST("/db_calendar/scheduler/save")
    Call<Void> save(@Body Event event);

}