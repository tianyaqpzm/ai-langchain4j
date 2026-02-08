package com.dark.aiagent.module.timekeeper.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "time_limit_events")
public class TimeLimitedEvent {

    @Id
    private String id;

    private String title;

    private String category;

    private Date date;

    private String time;

    private String description;

    private Boolean repeatYearly;

    private Appearance appearance;

    private Date createdAt;

    @Data
    public static class Appearance {
        private String type; // "image" or "color"
        private String value; // image path or hex code
    }
}
