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
