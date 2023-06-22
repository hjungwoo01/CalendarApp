package com.hjungwoo01.calendarapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.model.Event;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener, List<Event> events) {
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
        for (Event event : Event.eventsList) {
            try {
                LocalDate startDate = event.getStartDate();
                LocalDate endDate = event.getEndDate();
                LocalDate repeatEndDate = event.getRepeatEndDate();
                if (date.isEqual(startDate) || (date.isAfter(startDate) && date.isBefore(endDate)) || date.isEqual(endDate)) {
                    return true;
                }
                int repeatPosition = event.getRepeatPosition();
                // Check if the event is a repeating event
                if (repeatPosition != 0) {
                    LocalDate repeatStartDate = startDate;
                    LocalDate repeatIntervalEndDate = endDate;

                    while (repeatStartDate.isBefore(repeatEndDate)) {
                        repeatStartDate = getRepeatedStartDate(repeatStartDate, repeatPosition);
                        repeatIntervalEndDate = getRepeatedStartDate(repeatIntervalEndDate, repeatPosition);
                        if (date.isEqual(repeatStartDate) || date.isEqual(repeatIntervalEndDate) ||
                                (date.isAfter(repeatStartDate) && date.isBefore(repeatIntervalEndDate))) {
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

    private LocalDate getRepeatedStartDate(LocalDate startDate, int repeatPosition) {
        switch (repeatPosition) {
            case 1: // Every Day
                return startDate.plusDays(1);
            case 2: // Every Week
                return startDate.plusWeeks(1);
            case 3: // Every Month
                return startDate.plusMonths(1);
            case 4: // Every Year
                return startDate.plusYears(1);
            default:
                return startDate;
        }
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