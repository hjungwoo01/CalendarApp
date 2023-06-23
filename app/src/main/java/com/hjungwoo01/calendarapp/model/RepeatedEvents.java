package com.hjungwoo01.calendarapp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatedEvents {
    private final Event originalEvent;

    public static Map<Event, Event> repeatedEventsMap = new HashMap<>();

    public RepeatedEvents(Event originalEvent) {
        this.originalEvent = originalEvent;
    }

    public static ArrayList<Event> repeatedEventsForDate(LocalDate selectedDate) {
        ArrayList<Event> events = new ArrayList<>();
        for(Event e : repeatedEventsMap.keySet()) {
            if (e.getStartDate().equals(selectedDate) || (selectedDate.isAfter(e.getStartDate()) &&
                    selectedDate.isBefore(e.getEndDate().plusDays(1)))) {
                events.add(e);
            }
        }
        return events;
    }

    public List<Event> getRecurringEvents() {
        List<Event> recurringEvents = new ArrayList<>();
        int repeatPosition = originalEvent.getRepeatPosition();
        LocalDate startDate = getRepeatedStartDate(originalEvent.getStartDate(), repeatPosition);
        LocalDate endDate = getRepeatedStartDate(originalEvent.getEndDate(), repeatPosition);
        LocalDate repeatEndDate = originalEvent.getRepeatEndDate();

        while (startDate.isBefore(repeatEndDate)) {
            Event recurringEvent = new Event(originalEvent.getEventName(), originalEvent.getEventMemo(), startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + originalEvent.getStartTime(),
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + originalEvent.getEndTime(), originalEvent.getEventRepeat(), originalEvent.getEventEndRepeat());
            recurringEvents.add(recurringEvent);
            if(!repeatedEventsMap.containsKey(recurringEvent)) {
                repeatedEventsMap.put(recurringEvent, originalEvent);
            }

            startDate = getRepeatedStartDate(startDate, repeatPosition);
            endDate = getRepeatedStartDate(endDate, repeatPosition);
        }
        return recurringEvents;
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
}
