package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.File;

import java.util.List;

import okhttp3.ResponseBody;
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
    Call<ResponseBody> getFile(@Path("id") long id);

    @GET("/memos/files/getByMemoId/{memoId}")
    Call<ResponseBody> getFileByMemoId(@Path("memoId")long memoId);

    @POST("/memos/files/upload")
    Call<Void> upload(@Body File file);

    @DELETE("/memos/files/deleteByMemoId/{memoId}")
    Call<Void> deleteFileByMemoId(@Path("memoId") long memoId);

    @PUT("/memos/files/updateByMemoId/{memoId}")
    Call<Void> updateFileByMemoId(@Path("memoId") long memoId, @Body File file);
}
