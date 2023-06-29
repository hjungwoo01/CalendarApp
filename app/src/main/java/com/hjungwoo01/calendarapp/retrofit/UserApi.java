package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    @GET("/user/users/get-all")
    Call<List<User>> getAllUsers();

    @GET("/user/users/get/{id}")
    Call<User> getUser(@Path("id") long id);

    @POST("/user/users/save")
    Call<Void> save(@Body User user);

    @DELETE("/user/users/delete/{id}")
    Call<Void> deleteUser(@Path("id") long id);

    @PUT("/user/users/update/{id}")
    Call<Void> updateUser(@Path("id") long id, @Body User user);
}
