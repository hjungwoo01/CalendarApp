package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.File;
import com.hjungwoo01.calendarapp.model.Memo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FileApi {
    @GET("/memos/files/get-all")
    Call<List<File>> getAllFiles();

    @GET("/memos/files/get/{id}")
    Call<File> getFile(@Path("id") long id);

    @GET("/memos/files/getByMemoId/{memoId}")
    Call<File> getFileByMemoId(@Path("memoId")long memoId);

    @POST("/memos/files/upload")
    Call<Void> upload(@Body File file);

    @DELETE("/memos/files/delete/{id}")
    Call<Void> deleteFile(@Path("id") long id);

    @PUT("/memos/files/update/{id}")
    Call<Void> updateFile(@Path("id") long id, @Body File file);
}
