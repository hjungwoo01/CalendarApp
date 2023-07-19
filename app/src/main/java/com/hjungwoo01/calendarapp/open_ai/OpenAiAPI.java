package com.hjungwoo01.calendarapp.open_ai;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hjungwoo01.calendarapp.open_ai.data.Request;
import com.hjungwoo01.calendarapp.open_ai.data.OpenAiResponse;

import org.json.JSONException;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenAiAPI {

    private static final String TAG = "OpenAiAPI";
    private final String mUrl = "https://api.openai.com/v1/";


    //사용자 추천
    public void chat_gpt(Request request, final RetrofitCallback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(50000, TimeUnit.MILLISECONDS)
                .readTimeout(50000, TimeUnit.MILLISECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(mUrl)
                .client(okHttpClient)
                .build();
        OpenAiInterface apiInterface = retrofit.create(OpenAiInterface.class);
        Call<OpenAiResponse> call = apiInterface.chat_gpt(request);
        call.enqueue(new Callback<OpenAiResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenAiResponse> call, @NonNull Response<OpenAiResponse> response) {
                Log.d(TAG, "result: " + response.code() + " message : " + GSON.toJson(response.body()));
                if(response.isSuccessful()) {
                    try {
                        callback.onSuccess(response.code(), response.body());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                    callback.onFailure(response.code());

            }

            @Override
            public void onFailure(@NonNull Call<OpenAiResponse> call, @NonNull Throwable t) {
                Log.d(TAG, t.getMessage());
                callback.onError(t);

            }
        });
    }
}
