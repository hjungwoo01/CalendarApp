package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        initializeComponents();
    }
    private void initializeComponents() {
        TextInputEditText inputEditText = findViewById(R.id.form_textFieldEventName);
        TextInputEditText inputEditEventMemo = findViewById(R.id.form_textFieldEventMemo);
        TextInputEditText inputEditEventStart = findViewById(R.id.form_textFieldEventStart);
        TextInputEditText inputEditEventEnd = findViewById(R.id.form_textFieldEventEnd);
        TextInputEditText inputEditEventRepeat = findViewById(R.id.form_textFieldEventRepeat);
        MaterialButton buttonSave = findViewById(R.id.form_buttonSave);

        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        buttonSave.setOnClickListener(view -> {
            String eventName = String.valueOf(inputEditText.getText());
            String eventMemo = String.valueOf(inputEditEventMemo.getText());
            int eventStart = 0;
            int eventEnd = 0;
            int eventRepeat = 0;

            if (!TextUtils.isEmpty(inputEditEventStart.getText())) {
                eventStart = Integer.parseInt(String.valueOf(inputEditEventStart.getText()));
            }

            if (!TextUtils.isEmpty(inputEditEventEnd.getText())) {
                eventEnd = Integer.parseInt(String.valueOf(inputEditEventEnd.getText()));
            }

            if (!TextUtils.isEmpty(inputEditEventRepeat.getText())) {
                eventRepeat = Integer.parseInt(String.valueOf(inputEditEventRepeat.getText()));
            }

            Event event = new Event();
            event.setEventName(eventName);
            event.setEventMemo(eventMemo);
            event.setEventStart(eventStart);
            event.setEventEnd(eventEnd);
            event.setEventRepeat(eventRepeat);

            eventApi.save(event).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(EventActivity.this, "Save successful.", Toast.LENGTH_SHORT).show();
                        backToMonthView();

                    } else {
                        Toast.makeText(EventActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                        // Handle the failed response
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EventActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "Error occurred: " + t.getMessage());
                }
            });
        });
    }

    private void backToMonthView() {
        startActivity(new Intent(this, MainActivity.class));
    }
}