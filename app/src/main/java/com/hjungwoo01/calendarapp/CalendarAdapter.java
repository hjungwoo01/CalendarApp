package com.hjungwoo01.calendarapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.model.Event;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    private List<Event> events; // List to store the events
    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener, List<Event> events) {
        this.days = days;
        this.onItemListener = onItemListener;
        this.events = events;
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
            layoutParams.height = (int) parent.getHeight();

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
        for (Event event : events) {
            try {
                LocalDate eventDate = event.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (eventDate.equals(date)) {
                    return true;
                }
            } catch (ParseException e) {
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