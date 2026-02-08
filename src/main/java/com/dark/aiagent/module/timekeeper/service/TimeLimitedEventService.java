package com.dark.aiagent.module.timekeeper.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dark.aiagent.module.timekeeper.entity.TimeLimitedEvent;
import com.dark.aiagent.module.timekeeper.repository.TimeLimitedEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeLimitedEventService {

    private final TimeLimitedEventRepository timeLimitedEventRepository;

    public TimeLimitedEvent createEvent(TimeLimitedEvent event) {
        return timeLimitedEventRepository.save(event);
    }

    public List<TimeLimitedEvent> getAllEvents() {
        return timeLimitedEventRepository.findAll();
    }

    public Optional<TimeLimitedEvent> getEventById(String id) {
        return timeLimitedEventRepository.findById(id);
    }

    public TimeLimitedEvent updateEvent(String id, TimeLimitedEvent event) {
        event.setId(id);
        return timeLimitedEventRepository.save(event);
    }

    public void deleteEvent(String id) {
        timeLimitedEventRepository.deleteById(id);
    }
}
