package com.zhuchl.ailangchain4j.module.timekeeper.controller;

import com.zhuchl.ailangchain4j.module.timekeeper.entity.TimeLimitedEvent;
import com.zhuchl.ailangchain4j.module.timekeeper.service.TimeLimitedEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/dark/v1/time-limit-events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimeLimitedEventController {

    private final TimeLimitedEventService timeLimitedEventService;

    @PostMapping
    public ResponseEntity<TimeLimitedEvent> createEvent(@RequestBody TimeLimitedEvent event) {
        return ResponseEntity.ok(timeLimitedEventService.createEvent(event));
    }

    @GetMapping
    public ResponseEntity<List<TimeLimitedEvent>> getAllEvents() {
        return ResponseEntity.ok(timeLimitedEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeLimitedEvent> getEventById(@PathVariable String id) {
        return timeLimitedEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeLimitedEvent> updateEvent(@PathVariable String id, @RequestBody TimeLimitedEvent event) {
        return ResponseEntity.ok(timeLimitedEventService.updateEvent(id, event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        timeLimitedEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
