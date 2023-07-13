package com.hjungwoo01.calendarapp.scheduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjungwoo01.calendarapp.R;
import com.hjungwoo01.calendarapp.model.Event;

import java.util.List;
import java.util.Locale;

public class EventListAdapter extends ArrayAdapter<Event> {
    public EventListAdapter(@NonNull Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);

        TextView eventName = convertView.findViewById(R.id.eventName);
        TextView eventTime = convertView.findViewById(R.id.eventTime);

        String eventTimeString = "Start Time: " + makeTwoDigit(event.getStartHour()) + ":" + makeTwoDigit(event.getStartMinute())
                + "\nEnd Time: " + makeTwoDigit(event.getEndHour()) + ":" + makeTwoDigit(event.getEndMinute());

        eventName.setText(event.getEventName());
        eventTime.setText(eventTimeString);

        return convertView;
    }

    private String makeTwoDigit(int number) {
        return String.format(Locale.KOREA, "%02d", number);
    }
}