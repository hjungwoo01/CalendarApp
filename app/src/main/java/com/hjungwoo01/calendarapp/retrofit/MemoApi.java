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
    @GET("/memos/memos/get-all")
    Call<List<Memo>> getAllMemos();

    @GET("/memos/memos/get-by-owner/{owner}")
    Call<List<Memo>> getMemosByOwner(@Path("owner") String owner);

    @GET("/memos/memos/get-by-receiver/{receiver}")
    Call<List<Memo>> getMemosByReceiver(@Path("receiver")String receiver);
    @GET("/memos/memos/get/{id}")
    Call<Memo> getMemo(@Path("id") long id);

    @POST("/memos/memos/save")
    Call<Memo> save(@Body Memo memo);

    @DELETE("/memos/memos/delete/{id}")
    Call<Void> deleteMemo(@Path("id") long id);

    @PUT("/memos/memos/update/{id}")
    Call<Memo> updateMemo(@Path("id") long id, @Body Memo memo);
}
