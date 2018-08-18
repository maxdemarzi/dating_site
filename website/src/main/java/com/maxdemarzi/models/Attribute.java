package com.maxdemarzi.models;

import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Attribute {
    private Long id;
    private String name;
    private String lowercase_name;
    private String time;
    private Integer wants;
    private Integer has;
    private Boolean want;
    private Boolean have;

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String when() {
        ZonedDateTime dateTime = ZonedDateTime.parse(time);
        return dateFormat.format(dateTime);
    }
}
