package com.hjungwoo01.calendarapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.model.RepeatedEvents;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener) {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(days.size() > 15) //month view
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        else // week view
            layoutParams.height = parent.getHeight();

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final LocalDate date = days.get(position);
        if (date == null) {
            holder.dayOfMonth.setText("");
            holder.eventIndicator.setVisibility(View.INVISIBLE); // Hide event indicator for empty dates
        } else {
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            if (date.equals(CalendarUtils.selectedDate)) {
                holder.parentView.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.parentView.setBackgroundColor(Color.WHITE);
            }

            if (hasEventOnDate(date)) {
                holder.eventIndicator.setVisibility(View.VISIBLE); // Show event indicator for dates with events
            } else {
                holder.eventIndicator.setVisibility(View.INVISIBLE); // Hide event indicator for dates without events
            }

            holder.parentView.setOnClickListener(v -> {
                if (onItemListener != null) {
                    onItemListener.onItemClick(position, date);
                }
            });
        }
    }

    private boolean hasEventOnDate(LocalDate date) {
        for (Event event : Event.getEventsList()) {
            try {
                LocalDate startDate = event.getStartDate();
                LocalDate endDate = event.getEndDate();

                // Check if the date matches the original event
                if ((date.isEqual(startDate) || date.isAfter(startDate)) && date.isBefore(endDate.plusDays(1))) {
                    return true;
                }

                // Check if the event is a repeating event
                int repeatPosition = event.getRepeatPosition();
                if(repeatPosition != 0) {
                    RepeatedEvents repeatedEvents = new RepeatedEvents(event);
                    List<Event> recurringEvents = repeatedEvents.getRecurringEvents();

                    // Check if the date matches any recurring event's repetition
                    for (Event recurringEvent : recurringEvents) {
                        if ((date.isEqual(recurringEvent.getStartDate()) || date.isAfter(recurringEvent.getStartDate())) &&
                                date.isBefore(recurringEvent.getEndDate().plusDays(1))) {
                            return true;
                        }
                    }
                }

            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int getItemCount()
    {
        return days.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, LocalDate date);
    }
}