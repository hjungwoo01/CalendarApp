package com.hjungwoo01.calendarapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

public class EventActivity extends AppCompatActivity {
    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;
    private Button startDateButton;
    private Button endDateButton;
    private Button startTimeButton;
    private Button endTimeButton;
    private Spinner repeatEventSpinner;
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
        setContentView(R.layout.activity_event);
        repeatEventSpinner = findViewById(R.id.repeatEventSpinner);
        startDateButton = findViewById(R.id.startDatePickerButton);
        endDateButton = findViewById(R.id.endDatePickerButton);
        startTimeButton = findViewById(R.id.startTimePickerButton);
        endTimeButton = findViewById(R.id.endTimePickerButton);

        initStartDatePicker();
        initEndDatePicker();
        initRepeatInterval();
        initializeComponents();
        initLocalVariables();
        SwitchCompat allDayEventSwitch = findViewById(R.id.allDayEvent);

        allDayEventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Handle switch state change here
                if (isChecked) {
                    // Switch is ON (checked), all day event selected
                    // Perform necessary actions
                } else {
                    // Switch is OFF (unchecked), non-all day event selected
                    // Perform necessary actions
                }
            }
        });

        startDateButton.setText(getTodaysDate());
        startTimeButton.setText(getCurrentTime());
        endDateButton.setText(getTodaysDate());
        endTimeButton.setText(getCurrentTime());
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

    private void initLocalVariables() {
        Calendar cal = Calendar.getInstance();
        this.startYear = cal.get(Calendar.YEAR);
        this.startMonth = cal.get(Calendar.MONTH) + 1;
        this.startDay = cal.get(Calendar.DAY_OF_MONTH);
        this.startHour = cal.get(Calendar.HOUR);
        this.startMinute = cal.get(Calendar.MINUTE);
        this.endYear = cal.get(Calendar.YEAR);
        this.endMonth = cal.get(Calendar.MONTH) + 1;
        this.endDay = cal.get(Calendar.DAY_OF_MONTH);
        this.endHour = cal.get(Calendar.HOUR);
        this.endMinute = cal.get(Calendar.MINUTE);
    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        return makeTwoDigit(hour) + ":" + makeTwoDigit(minute);
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeTwoDigit(month) + "/" + makeTwoDigit(day) + "/" + year;
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
        String stringMonth = makeTwoDigit(month);
        return stringMonth + "/" + day + "/" + year;
    }

    private String makeTwoDigit(int number) {
        if(number < 10) {
            return "0" + number;
        }
        return number + "";
    }

    private String eventStartString() {
        return this.startYear + "" + makeTwoDigit(this.startMonth) + "" + makeTwoDigit(this.startDay) + "" + makeTwoDigit(this.startHour) + "" + makeTwoDigit(this.startMinute);
    }

    private String eventEndString() {
        return this.endYear + "" + makeTwoDigit(this.endMonth) + "" + makeTwoDigit(this.endDay) + "" + makeTwoDigit(this.endHour) + "" + makeTwoDigit(this.endMinute);
    }

    public String getRepeatInterval() {
        return this.repeatInterval;
    }

    private void initializeComponents() {
        TextInputEditText inputEditText = findViewById(R.id.form_textFieldEventName);
        TextInputEditText inputEditEventMemo = findViewById(R.id.form_textFieldEventMemo);
        MaterialButton buttonSave = findViewById(R.id.form_buttonSave);

        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        buttonSave.setOnClickListener(view -> {
            String eventName = String.valueOf(inputEditText.getText());
            String eventMemo = String.valueOf(inputEditEventMemo.getText());
            String eventStart = this.eventStartString();
            String eventEnd = this.eventEndString();
            String eventRepeat = getRepeatInterval();


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

                        // Finish the activity to return to the MainActivity
                        finish();
                    } else {
                        Toast.makeText(EventActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EventActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    Log.e("EventActivity", "Error occurred: " + t.getMessage());
                }
            });
        });
    }

    public void backToMain(View view) {
        startActivity(new Intent(EventActivity.this, MainActivity.class));
    }

    public void openStartDatePicker(View view) {
        startDatePickerDialog.setTitle("Select Start Date");
        startDatePickerDialog.show();
    }

    public void openEndDatePicker(View view) {
        endDatePickerDialog.setTitle("Select End Date");
        endDatePickerDialog.show();
    }

}