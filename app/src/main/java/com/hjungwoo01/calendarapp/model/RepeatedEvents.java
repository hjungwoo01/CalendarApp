package com.hjungwoo01.calendarapp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepeatedEvents {
    private final Event baseEvent;

    public static Map<Event, Event> repeatedEventsMap = new ConcurrentHashMap<>();

    public RepeatedEvents(Event baseEvent) {
        this.baseEvent = baseEvent;
    }

    public static List<Event> repeatedEventsForDate(LocalDate selectedDate) {
        List<Event> events = new ArrayList<>();
        for (Event e : repeatedEventsMap.keySet()) {
            if (e.getStartDate().equals(selectedDate) || (selectedDate.isAfter(e.getStartDate()) &&
                    selectedDate.isBefore(e.getEndDate().plusDays(1)))) {
                events.add(e);
            }
        }
        return events;
    }

    public List<Event> getRecurringEvents() {
        List<Event> recurringEvents = new ArrayList<>();
        int repeatPosition = baseEvent.getRepeatPosition();
        LocalDate startDate = getRepeatedDate(baseEvent.getStartDate(), repeatPosition);
        LocalDate endDate = getRepeatedDate(baseEvent.getEndDate(), repeatPosition);
        LocalDate repeatEndDate = baseEvent.getRepeatEndDate();

        while (startDate.isBefore(repeatEndDate)) {
            Event recurringEvent = new Event(
                    baseEvent.getEventName(),
                    baseEvent.getEventMemo(),
                    formatDateAndTime(startDate, baseEvent.getStartTime()),
                    formatDateAndTime(endDate, baseEvent.getEndTime()),
                    baseEvent.getEventRepeat(),
                    baseEvent.getEventEndRepeat()
            );
            recurringEvents.add(recurringEvent);
            if (!repeatedEventsMap.containsKey(recurringEvent)) {
                repeatedEventsMap.put(recurringEvent, baseEvent);
            }

            startDate = getRepeatedDate(startDate, repeatPosition);
            endDate = getRepeatedDate(endDate, repeatPosition);
        }
        return recurringEvents;
    }

    private LocalDate getRepeatedDate(LocalDate date, int repeatPosition) {
        switch (repeatPosition) {
            case 1: // Every Day
                return date.plusDays(1);
            case 2: // Every Week
                return date.plusWeeks(1);
            case 3: // Every Month
                return date.plusMonths(1);
            case 4: // Every Year
                return date.plusYears(1);
            default:
                return date;
        }
    }

    private String formatDateAndTime(LocalDate date, String time) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + time;
    }
}
