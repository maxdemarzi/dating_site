package com.maxdemarzi.models;

import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Thing {
    private Long id;
    private String name;
    private String lowercase_name;
    private String time;
    private Integer likes;
    private Integer hates;
    private Boolean liked;
    private Boolean hated;
    private Boolean shared;

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String when() {
        ZonedDateTime dateTime = ZonedDateTime.parse(time);
        return dateFormat.format(dateTime);
    }

}
