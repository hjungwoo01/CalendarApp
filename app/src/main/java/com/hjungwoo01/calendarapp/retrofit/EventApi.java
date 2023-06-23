package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventApi {
    @GET("/db_calendar/scheduler/get-all")
    Call<List<Event>> getAllEvents();

    @GET("/db_calendar/scheduler/get-by-owner/{owner}")
    Call<List<Event>> getEventsByOwner(@Path("owner") String owner);
    @GET("/db_calendar/scheduler/get/{id}")
    Call<Event> getEvent(@Path("id") long id);

    @POST("/db_calendar/scheduler/save")
    Call<Void> save(@Body Event event);

    @DELETE("/db_calendar/scheduler/delete/{id}")
    Call<Void> deleteEvent(@Path("id") long id);

    @PUT("/db_calendar/scheduler/update/{id}")
    Call<Void> updateEvent(@Path("id") long id, @Body Event event);

}