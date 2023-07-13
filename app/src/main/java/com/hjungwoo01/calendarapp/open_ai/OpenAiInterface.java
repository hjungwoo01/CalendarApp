package com.hjungwoo01.calendarapp.open_ai;

import com.hjungwoo01.calendarapp.open_ai.data.OpenAiResponse;
import com.hjungwoo01.calendarapp.open_ai.data.Request;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAiInterface {
    @Headers({"Content-Type: application/json", "Authorization: Bearer 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"})
    @POST("chat/completions")
    Call<OpenAiResponse> chat_gpt (@Body Request request);
}
