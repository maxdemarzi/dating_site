package com.maxdemarzi.models;

import humanize.Humanize;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
public class Message {
    private String author;
    private String status;
    private String time;

    public String humanTime() {
        return Humanize.naturalTime(Date.from(ZonedDateTime.parse(time).toInstant()));
    }

}
