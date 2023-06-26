package com.hjungwoo01.calendarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.hjungwoo01.calendarapp.CalendarUtils.daysInMonthArray;
import static com.hjungwoo01.calendarapp.CalendarUtils.monthYearFromDate;
import static com.hjungwoo01.calendarapp.model.RepeatedEvents.repeatedEventsMap;

import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.model.RepeatedEvents;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        CalendarUtils.selectedDate = LocalDate.now();
        setMonthView();
        fetchEvents();
    }


    @Override
    protected void onResume() {
        super.onResume();
        fetchEvents();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void fetchEvents() {
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);

        Call<List<Event>> call = eventApi.getEventsByOwner(OwnerSelectionActivity.getSelectedOwner());
        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (response.isSuccessful()) {
                    Event.eventsList = response.body();
                    setMonthView();
                } else {
                    Toast.makeText(MainActivity.this, "Empty calendar.", Toast.LENGTH_SHORT).show();
                    Event.eventsList = new ArrayList<>();
                    RepeatedEvents.repeatedEventsMap = new HashMap<>();
                    setMonthView();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
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
        if (date != null) {
            CalendarUtils.selectedDate = date;
            setMonthView();

            Event clickedEvent = getClickedEvent(date);

            if (clickedEvent != null) {
                if(Event.eventsForDate(CalendarUtils.selectedDate).size() +
                        RepeatedEvents.repeatedEventsForDate(CalendarUtils.selectedDate).size() > 1) {
                    startActivity(new Intent(MainActivity.this, EventListActivity.class));
                } else {
                    long eventId = Objects.requireNonNull(repeatedEventsMap.getOrDefault(clickedEvent, clickedEvent)).getId();

                    Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(MainActivity.this, "No event on this date", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void newEventAction(View view) {
        startActivity(new Intent(MainActivity.this, EventActivity.class));
    }

    private Event getClickedEvent(LocalDate date) {
        for (Event event : Event.eventsList) {
            LocalDate eventStartDate = LocalDate.parse(event.getEventStart(), DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            LocalDate eventEndDate = LocalDate.parse(event.getEventEnd(), DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            if (eventStartDate.equals(date) || date.isAfter(eventStartDate) && date.isBefore(eventEndDate.plusDays(1))) {
                return event;
            }
        }

        for(Event e : repeatedEventsMap.keySet()) {
            LocalDate repeatedEventStartDate = LocalDate.parse(e.getEventStart(), DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            LocalDate repeatedEventEndDate = LocalDate.parse(e.getEventEnd(), DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            if (repeatedEventStartDate.equals(date) || date.isAfter(repeatedEventStartDate) && date.isBefore(repeatedEventEndDate.plusDays(1))) {
                return repeatedEventsMap.get(e);
            }
        }
        return null;
    }
    public void changeOwner(View view) {
        startActivity(new Intent(MainActivity.this, OwnerSelectionActivity.class));
    }
}
