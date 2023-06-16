package com.hjungwoo01.calendarapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {

    private Event event;
    private TextInputEditText inputEditText;
    private TextInputEditText inputEditEventMemo;
    private TextInputEditText inputEditEventStart;
    private TextInputEditText inputEditEventEnd;
    private TextInputEditText inputEditEventRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        inputEditText = findViewById(R.id.form_textFieldEventName);
        inputEditEventMemo = findViewById(R.id.form_textFieldEventMemo);
        inputEditEventStart = findViewById(R.id.form_textFieldEventStart);
        inputEditEventEnd = findViewById(R.id.form_textFieldEventEnd);
        inputEditEventRepeat = findViewById(R.id.form_textFieldEventRepeat);

        // Retrieve the event ID from the intent extras
        long eventId = getIntent().getLongExtra("eventId", -1);

        if (eventId != -1) {
            // Fetch the event details from the API
            fetchEventDetails(eventId);

            MaterialButton updateButton = findViewById(R.id.form_buttonUpdate);

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String eventName = String.valueOf(inputEditText.getText());
                    String eventMemo = String.valueOf(inputEditEventMemo.getText());
                    String eventStart = String.valueOf(inputEditEventStart.getText());
                    String eventEnd = String.valueOf(inputEditEventEnd.getText());
                    String eventRepeat = String.valueOf(inputEditEventRepeat.getText());

                    if(!eventName.isEmpty()) {
                        Event updatedEvent = new Event();
                        updatedEvent.setEventName(eventName);
                        updatedEvent.setEventMemo(eventMemo);
                        updatedEvent.setEventStart(eventStart);
                        updatedEvent.setEventEnd(eventEnd);
                        updatedEvent.setEventRepeat(eventRepeat);

                        updateEvent(eventId, updatedEvent);
                    } else {
                        // Show validation error message
                        Toast.makeText(EventDetailsActivity.this, "Invalid input. Please check your inputs and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Handle the delete button click
            MaterialButton deleteButton = findViewById(R.id.form_buttonDelete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(eventId);
                }
            });
        } else {
            // Handle the case when the event ID is not provided
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchEventDetails(long eventId) {
        // Call the API to fetch the event details by ID
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        eventApi.getEvent(eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful()) {
                    // Event details fetched successfully
                    event = response.body();
                    // Check if the event is not null
                    if (event != null) {
                        // Display the event details in your views
                        displayEventDetails();
                    } else {
                        // Handle the case when the event is null
                        Toast.makeText(EventDetailsActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Failed to fetch event details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Toast.makeText(EventDetailsActivity.this, "Failed to fetch event details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void displayEventDetails() {
        if(event != null) {
            if (event.getEventName() != null) {
                inputEditText.setText(event.getEventName());
            }
            if (event.getEventMemo() != null) {
                inputEditEventMemo.setText(event.getEventMemo());
            }
            if (event.getEventStart() != null) {
                inputEditEventStart.setText(event.getEventStart());
            }
            if (event.getEventEnd() != null) {
                inputEditEventEnd.setText(event.getEventEnd());
            }
            if (event.getEventRepeat() != null) {
                inputEditEventRepeat.setText(event.getEventRepeat());
            }
        }
    }

    private void updateEvent(long eventId, Event event) {
        // Update the existing event using the PUT method
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        eventApi.updateEvent(eventId, event).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailsActivity.this, "Update successful.", Toast.LENGTH_SHORT).show();

                    // Finish the activity to return to the MainActivity
                    finish();
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                    // Handle failed response
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EventDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                Log.e("EventDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(long eventId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEvent(eventId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteEvent(long eventId) {
        // Call the deleteEvent API method with the event id
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);
        eventApi.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailsActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();

                    // Finish the activity to return to the MainActivity
                    finish();
                } else {
                    // Handle API error
                    // Display an error message or retry the operation
                    Toast.makeText(EventDetailsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle network or unexpected errors
                // Display an error message or retry the operation
                Toast.makeText(EventDetailsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                Log.e("EventDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }
}
