package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.Memo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MemoApi {
    @GET("/db_calendar/memos/get-all")
    Call<List<Memo>> getAllMemos();

    @GET("/db_calendar/memos/get-by-owner/{owner}")
    Call<List<Memo>> getMemosByOwner(@Path("owner") String owner);

    @GET("/db_calendar/memos/get-by-receiver/{receiver}")
    Call<List<Memo>> getMemosByReceiver(@Path("receiver")String receiver);
    @GET("/db_calendar/memos/get/{id}")
    Call<Memo> getMemo(@Path("id") long id);

    @POST("/db_calendar/memos/save")
    Call<Void> save(@Body Memo memo);

    @DELETE("/db_calendar/memos/delete/{id}")
    Call<Void> deleteMemo(@Path("id") long id);

    @PUT("/db_calendar/memos/update/{id}")
    Call<Void> updateMemo(@Path("id") long id, @Body Memo memo);

}
