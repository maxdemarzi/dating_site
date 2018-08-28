package com.maxdemarzi.models;

import humanize.Humanize;
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
public class Post {
    private Long id;
    private String status;
    private String name;
    private String name2;
    private String username;
    private String username2;
    private String filename;
    private String hash;
    private String hash2;
    private String time;
    private Integer low_fives;
    private boolean low_fived;
    private Integer high_fives;
    private boolean high_fived;

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String when() {
        ZonedDateTime dateTime = ZonedDateTime.parse(time);
        return dateFormat.format(dateTime);
    }

    public String humanTime() {
        return Humanize.naturalTime(Date.from(ZonedDateTime.parse(time).toInstant()));
    }

    public String expires() {
        return Humanize.naturalTime(Date.from(ZonedDateTime.parse(time).plusDays(5).toInstant()));
    }

    public boolean hasFile() {
        return filename != null;
    }

}
