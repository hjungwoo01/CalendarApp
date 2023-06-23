package com.hjungwoo01.calendarapp;

import static com.hjungwoo01.calendarapp.model.RepeatedEvents.repeatedEventsMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.model.RepeatedEvents;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class EventListActivity extends AppCompatActivity {
    private ListView eventListView;
    private TextView eventsOnDate;
    private List<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        initWidgets();
        events = Event.eventsForDate(CalendarUtils.selectedDate);
        events.addAll(RepeatedEvents.repeatedEventsForDate(CalendarUtils.selectedDate));
        fetchEventList();
    }

    private List<Event> sortEvents(List<Event> events) {
        events.sort(new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                if(e1.getStartHour() > e2.getStartHour() ||
                        (e1.getStartHour() == e2.getStartHour() && e1.getStartMinute() > e2.getStartMinute())) {
                    return 1;
                } else if(e1.getStartMinute() == e2.getStartMinute()) {
                    return 0;
                }
                return -1;
            }
        });
        return events;
    }

    private void fetchEventList() {
        EventListAdapter adapter = new EventListAdapter(this, sortEvents(this.events));
        eventListView.setAdapter(adapter);
        eventsOnDate.setText(CalendarUtils.formattedDate(CalendarUtils.selectedDate));
    }

    private void initWidgets() {
        eventListView = findViewById(R.id.eventListView);
        eventsOnDate = findViewById(R.id.eventsOnDate);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event) parent.getItemAtPosition(position);
                showEventDetails(selectedEvent);
            }
        });
    }

    private void showEventDetails(Event event) {
        Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
        intent.putExtra("eventId", Objects.requireNonNull(repeatedEventsMap.getOrDefault(event, event)).getId());
        startActivity(intent);
    }

    public void backToMain(View view) {
        startActivity(new Intent(EventListActivity.this, MainActivity.class));
    }
}
