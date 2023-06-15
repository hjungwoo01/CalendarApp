package com.hjungwoo01.calendarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.hjungwoo01.calendarapp.CalendarUtils.daysInMonthArray;
import static com.hjungwoo01.calendarapp.CalendarUtils.monthYearFromDate;

import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    private List<Event> events; // List to store the events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        CalendarUtils.selectedDate = LocalDate.now();
        setMonthView();
        events = new ArrayList<>();
        fetchEvents();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, events);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void fetchEvents() {
        // Make an API call or retrieve events from the database
        // Update the 'events' list with the fetched events
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        Call<List<Event>> call = eventApi.getAllEvents();
        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful()) {
                    // Clear the existing events list
                    events.clear();

                    // Add the fetched events to the events list
                    assert response.body() != null;
                    events.addAll(response.body());

                    // Refresh the calendar view to display the events
                    setMonthView();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    public void previousMonthAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date != null) {
            CalendarUtils.selectedDate = date;
            setMonthView();
        }
    }

    public void newEventAction(View view) {
        startActivity(new Intent(this, EventActivity.class));
    }

}