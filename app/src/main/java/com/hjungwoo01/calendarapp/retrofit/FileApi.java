package com.hjungwoo01.calendarapp.retrofit;

import com.hjungwoo01.calendarapp.model.File;

import java.util.stream.Stream;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FileApi {
    @GET("/memos/files/get-all")
    Call<Stream<File>> getAllFiles();

    @GET("/memos/files/get/{id}")
    Call<File> getFile(@Path("id") String id);

    @Multipart
    @POST("/memos/files/upload")
    Call<Void> upload(@Body File file);

    @DELETE("/memos/files/delete/{id}")
    Call<Void> deleteFile(@Path("id") String id);

    @PUT("/memos/files/update/{id}")
    Call<Void> updateFile(@Path("id") String id, @Body File file);
}
