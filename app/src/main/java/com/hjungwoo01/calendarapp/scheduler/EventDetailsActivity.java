package com.hjungwoo01.calendarapp.scheduler;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hjungwoo01.calendarapp.OwnerSelectionActivity;
import com.hjungwoo01.calendarapp.R;
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
    private TextInputEditText inputEditEventName;
    private TextInputEditText inputEditEventMemo;
    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;
    private DatePickerDialog repeatEndDatePickerDialog;
    private Button startDateButton;
    private Button endDateButton;
    private Button startTimeButton;
    private Button endTimeButton;
    private Button repeatEndDateButton;
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
    private int repeatEndYear;
    private int repeatEndMonth;
    private int repeatEndDay;
    private LinearLayout eventRepeatEndBlock;
    private String repeatInterval;
    private final String[] intervalOptions = {"Never", "Every Day", "Every Week", "Every Month", "Every Year"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        initWidgets();
        initStartDatePicker();
        initEndDatePicker();
        initRepeatEndDatePicker();
        initRepeatInterval();


        long eventId = getIntent().getLongExtra("eventId", -1);

        if (eventId != -1) {
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

                    String eventName = String.valueOf(inputEditEventName.getText());
                    String eventMemo = String.valueOf(inputEditEventMemo.getText());
                    String eventStart = getEventStartString();
                    String eventEnd = getEventEndString();
                    String eventRepeat = getRepeatInterval();
                    String eventEndRepeat = getRepeatEndString();

                    if(!eventName.isEmpty()) {
                        Event updatedEvent = new Event();
                        updatedEvent.setOwner(OwnerSelectionActivity.getSelectedOwner());
                        updatedEvent.setEventName(eventName);
                        updatedEvent.setEventMemo(eventMemo);
                        updatedEvent.setEventStart(eventStart);
                        updatedEvent.setEventEnd(eventEnd);
                        updatedEvent.setEventRepeat(eventRepeat);
                        updatedEvent.setEventEndRepeat(eventEndRepeat);

                        updateEvent(eventId, updatedEvent);
                    } else {
                        Toast.makeText(EventDetailsActivity.this, "Invalid input. Please check your inputs and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            MaterialButton deleteButton = findViewById(R.id.form_buttonDelete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(eventId);
                }
            });
        } else {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initWidgets() {
        inputEditEventName = findViewById(R.id.form_textFieldEventName);
        inputEditEventMemo = findViewById(R.id.form_textFieldEventMemo);
        repeatEventSpinner = findViewById(R.id.repeatEventSpinner);
        startDateButton = findViewById(R.id.startDatePickerButton);
        endDateButton = findViewById(R.id.endDatePickerButton);
        startTimeButton = findViewById(R.id.startTimePickerButton);
        endTimeButton = findViewById(R.id.endTimePickerButton);
        allDayEventSwitch = findViewById(R.id.allDayEvent);
        repeatEndDateButton = findViewById(R.id.repeatEndDatePickerButton);
        eventRepeatEndBlock = findViewById(R.id.eventRepeatEndBlock);
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
                if(position != 0) {
                    eventRepeatEndBlock.setVisibility(View.VISIBLE);
                    repeatEndDateButton.setText(makeDateString(repeatEndDay, repeatEndMonth, repeatEndYear));
                    if(event.getEventEndRepeat().equals("00000")) {
                        repeatEndDateButton.setText(getStartDateString());
                    }
                    initRepeatEndDatePicker();
                } else {
                    eventRepeatEndBlock.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                repeatInterval = intervalOptions[0];
            }
        });
    }

    private String getStartDateString() {
        return makeTwoDigit(this.startMonth) + "/" + makeTwoDigit(this.startDay) + "/" + this.startYear;
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

        startDatePickerDialog = new DatePickerDialog(this, /* style, */ dateSetListener, year, month, day);
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

        endDatePickerDialog = new DatePickerDialog(this, /* style, */ dateSetListener, year, month, day);
    }

    private void initRepeatEndDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month += 1;
                repeatEndYear = year;
                repeatEndMonth = month;
                repeatEndDay = day;
                String date = makeDateString(day, month, year);
                repeatEndDateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        repeatEndDatePickerDialog = new DatePickerDialog(this, /* style, */ dateSetListener, year, month, day);
    }

    public void openStartDatePicker(View view) {
        startDatePickerDialog.setTitle("Select Start Date");
        startDatePickerDialog.show();
    }

    public void openEndDatePicker(View view) {
        endDatePickerDialog.setTitle("Select End Date");
        endDatePickerDialog.show();
    }

    public void openRepeatEndDatePicker(View view) {
        repeatEndDatePickerDialog.setTitle("Select Repeat End Date");
        repeatEndDatePickerDialog.show();
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, /* style, */ onTimeSetListener, startHour, startMinute, true);

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
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, /* style, */ onTimeSetListener, endHour, endMinute, true);

        timePickerDialog.setTitle("Select End Time");
        timePickerDialog.show();
    }
    private String makeDateString(int day, int month, int year) {
        return makeTwoDigit(month) + "/" + makeTwoDigit(day) + "/" + year;
    }
    private String makeTwoDigit(int number) {
        return String.format(Locale.KOREA, "%02d", number);
    }
    private String getEventStartString() {
        return this.startYear + "" + makeTwoDigit(this.startMonth) + "" + makeTwoDigit(this.startDay) + "" + makeTwoDigit(this.startHour) + "" + makeTwoDigit(this.startMinute);
    }
    private String getEventEndString() {
        return this.endYear + "" + makeTwoDigit(this.endMonth) + "" + makeTwoDigit(this.endDay) + "" + makeTwoDigit(this.endHour) + "" + makeTwoDigit(this.endMinute);
    }

    private String getRepeatEndString() {
        return this.repeatEndYear + "" + makeTwoDigit(this.repeatEndMonth) + makeTwoDigit(this.repeatEndDay);
    }
    public String getRepeatInterval() {
        return this.repeatInterval;
    }
    private void displayEventDetails() {
        setLocalVariables(this.event);
        if(event.getEventName() != null) {
            inputEditEventName.setText(event.getEventName());
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
            repeatEventSpinner.setSelection(event.getRepeatPosition());
            if (event.getRepeatPosition() != 0) {
                eventRepeatEndBlock.setVisibility(View.VISIBLE);
                if (event.getEventEndRepeat() != null) {
                    repeatEndDateButton.setText(makeDateString(repeatEndDay, repeatEndMonth, repeatEndYear));
                }
            }
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
        this.repeatEndYear = event.getRepeatEndYear();
        this.repeatEndMonth = event.getRepeatEndMonth();
        this.repeatEndDay = event.getRepeatEndDay();
    }

    private void fetchEventDetails(long eventId) {
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        eventApi.getEvent(eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(@NonNull Call<Event> call, @NonNull Response<Event> response) {
                if (response.isSuccessful()) {
                    event = response.body();

                    if (event != null) {
                        displayEventDetails();
                    } else {
                        Toast.makeText(EventDetailsActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Failed to fetch event details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Event> call, @NonNull Throwable t) {
                Toast.makeText(EventDetailsActivity.this, "Failed to fetch event details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEvent(long eventId, Event event) {
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        eventApi.updateEvent(eventId, event).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailsActivity.this, "Update successful.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(EventDetailsActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
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
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);
        eventApi.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailsActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(EventDetailsActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(EventDetailsActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                Log.e("EventDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    public void backToMain(View view) {
        startActivity(new Intent(EventDetailsActivity.this, MainActivity.class));
    }
}
