package com.zhuchl.ailangchain4j.module.timekeeper.service;

import com.zhuchl.ailangchain4j.module.timekeeper.entity.TimeLimitedEvent;
import com.zhuchl.ailangchain4j.module.timekeeper.repository.TimeLimitedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
