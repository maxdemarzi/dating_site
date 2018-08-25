package com.maxdemarzi.models;

import humanize.Humanize;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
public class Conversation {
    private String name;
    private String username;
    private String hash;
    private String author;
    private String status;
    private String time;

    public String humanTime() {
        return Humanize.naturalTime(Date.from(ZonedDateTime.parse(time).toInstant()));
    }

    public String preview() {
        return status.substring(0, Math.min(status.length(), 40));
    }

}
