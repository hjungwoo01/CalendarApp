package com.hjungwoo01.calendarapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {

    private Event event;
    private TextInputEditText inputEditText;
    private TextInputEditText inputEditEventMemo;
    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;
    private Button startDateButton;
    private Button endDateButton;
    private Button startTimeButton;
    private Button endTimeButton;
    private Spinner repeatEventSpinner;

    private SwitchCompat allDayEventSwitch;
    private int startYear;
    private int startMonth;
    private int startDay;
    private int startHour;
    private int startMinute;
    private int endYear;
    private int endMonth;
    private int endDay;
    private int endHour;
    private int endMinute;
    private String repeatInterval;
    private String[] intervalOptions = {"Never", "Every Day", "Every Week", "Every Month", "Every Year"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        inputEditText = findViewById(R.id.form_textFieldEventName);
        inputEditEventMemo = findViewById(R.id.form_textFieldEventMemo);
        repeatEventSpinner = findViewById(R.id.repeatEventSpinner);
        startDateButton = findViewById(R.id.startDatePickerButton);
        endDateButton = findViewById(R.id.endDatePickerButton);
        startTimeButton = findViewById(R.id.startTimePickerButton);
        endTimeButton = findViewById(R.id.endTimePickerButton);
        allDayEventSwitch = findViewById(R.id.allDayEvent);

        initStartDatePicker();
        initEndDatePicker();
        initRepeatInterval();



        // Retrieve the event ID from the intent extras
        long eventId = getIntent().getLongExtra("eventId", -1);

        if (eventId != -1) {
            // Fetch the event details from the API
            fetchEventDetails(eventId);

            allDayEventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Switch is ON (checked), all day event selected
                        startTimeButton.setVisibility(View.GONE);
                        endTimeButton.setVisibility(View.GONE);
                        endDateButton.setVisibility(View.GONE);
                    } else {
                        // Switch is OFF (unchecked), non-all day event selected
                        startTimeButton.setVisibility(View.VISIBLE);
                        endTimeButton.setVisibility(View.VISIBLE);
                        endDateButton.setVisibility(View.VISIBLE);
                        String startTime = makeTwoDigit(startHour) + ":" + makeTwoDigit(startMinute);
                        startTimeButton.setText(startTime);
                        String endTime = makeTwoDigit(endHour) + ":" + makeTwoDigit(endMinute);
                        endTimeButton.setText(endTime);
                        endDateButton.setText(makeDateString(endDay, endMonth, endYear));
                    }
                }
            });

            MaterialButton updateButton = findViewById(R.id.form_buttonUpdate);

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(allDayEventSwitch.isChecked()) {
                        endYear = startYear;
                        endMonth = startMonth;
                        endDay = startDay;
                        startHour = 0;
                        startMinute = 0;
                        endHour = 23;
                        endMinute = 59;
                    }
                    String eventName = String.valueOf(inputEditText.getText());
                    String eventMemo = String.valueOf(inputEditEventMemo.getText());
                    String eventStart = getEventStartString();
                    String eventEnd = getEventEndString();
                    String eventRepeat = getRepeatInterval();

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

    private void initRepeatInterval() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervalOptions, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatEventSpinner.setAdapter(adapter);

        repeatEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeatInterval = intervalOptions[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                repeatInterval = intervalOptions[0];
            }
        });
    }

    private int getRepeatPosition() {
        for(int i = 0; i < intervalOptions.length; i++) {
            if(intervalOptions[i].equals(getRepeatInterval())) {
                return i;
            }
        }
        return 0;
    }

    // Date picker
    private void initStartDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month += 1;
                startYear = year;
                startMonth = month;
                startDay = day;
                String date = makeDateString(day, month, year);
                startDateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        startDatePickerDialog = new DatePickerDialog(this, /*style,*/ dateSetListener, year, month, day);
    }

    private void initEndDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month += 1;
                endYear = year;
                endMonth = month;
                endDay = day;
                String date = makeDateString(day, month, year);
                endDateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        endDatePickerDialog = new DatePickerDialog(this, /*style,*/ dateSetListener, year, month, day);
    }

    public void openStartDatePicker(View view) {
        startDatePickerDialog.setTitle("Select Start Date");
        startDatePickerDialog.show();
    }

    public void openEndDatePicker(View view) {
        endDatePickerDialog.setTitle("Select End Date");
        endDatePickerDialog.show();
    }
    // Time picker
    public void openStartTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                startHour = selectedHour;
                startMinute = selectedMinute;
                startTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d",startHour, startMinute));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, /*style,*/ onTimeSetListener, startHour, startMinute, true);

        timePickerDialog.setTitle("Select Start Time");
        timePickerDialog.show();
    }

    public void openEndTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                endHour = selectedHour;
                endMinute = selectedMinute;
                endTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d",endHour, endMinute));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, /*style,*/ onTimeSetListener, endHour, endMinute, true);

        timePickerDialog.setTitle("Select End Time");
        timePickerDialog.show();
    }

    private String makeDateString(int day, int month, int year) {
        return makeTwoDigit(month) + "/" + makeTwoDigit(day) + "/" + year;
    }

    private String makeTwoDigit(int number) {
        if(number < 10) {
            return "0" + number;
        }
        return number + "";
    }

    private String getEventStartString() {
        return this.startYear + "" + makeTwoDigit(this.startMonth) + "" + makeTwoDigit(this.startDay) + "" + makeTwoDigit(this.startHour) + "" + makeTwoDigit(this.startMinute);
    }

    private String getEventEndString() {
        return this.endYear + "" + makeTwoDigit(this.endMonth) + "" + makeTwoDigit(this.endDay) + "" + makeTwoDigit(this.endHour) + "" + makeTwoDigit(this.endMinute);
    }

    public String getRepeatInterval() {
        return this.repeatInterval;
    }

    private void displayEventDetails() {
        setLocalVariables(this.event);

        if(event.getEventName() != null) {
            inputEditText.setText(event.getEventName());
        }
        if(event.getEventMemo() != null) {
            inputEditEventMemo.setText(event.getEventMemo());
        }
        if(event.isAllDay()) {
            allDayEventSwitch.setChecked(true);
            if(event.getEventStart() != null) {
                startDateButton.setText(makeDateString(startDay, startMonth, startYear));
                startTimeButton.setVisibility(View.GONE);
                endDateButton.setVisibility(View.GONE);
                endTimeButton.setVisibility(View.GONE);
            }
        } else {
            allDayEventSwitch.setChecked(false);
            if (event.getEventStart() != null) {
                startDateButton.setText(makeDateString(startDay, startMonth, startYear));
                String startTime = makeTwoDigit(startHour) + ":" + makeTwoDigit(startMinute);
                startTimeButton.setText(startTime);
            }

            if (event.getEventEnd() != null) {
                endDateButton.setText(makeDateString(endDay, endMonth, endYear));
                String endTime = makeTwoDigit(endHour) + ":" + makeTwoDigit(endMinute);
                endTimeButton.setText(endTime);
            }
        }
        if(event.getEventRepeat() != null) {
            repeatEventSpinner.setSelection(getRepeatPosition());
        }
    }

    private void setLocalVariables(Event event) {
        this.startYear = event.getStartYear();
        this.startMonth = event.getStartMonth();
        this.startDay = event.getStartDay();
        this.startHour = event.getStartHour();
        this.startMinute = event.getStartMinute();
        this.endYear = event.getEndYear();
        this.endMonth = event.getEndMonth();
        this.endDay = event.getEndDay();
        this.endHour = event.getEndHour();
        this.endMinute = event.getEndMinute();
        this.repeatInterval = event.getEventRepeat();
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

    public void backToMain(View view) {
        startActivity(new Intent(EventDetailsActivity.this, MainActivity.class));
    }
}
