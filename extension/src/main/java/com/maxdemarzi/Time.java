package com.maxdemarzi;

import javax.ws.rs.QueryParam;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Time {
    public static final ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy_MM_dd")
            .withZone(utc);


    public static ZonedDateTime getLatestTime(@QueryParam("since") String since) {
        ZonedDateTime latest;
        if (since == null) {
            latest = ZonedDateTime.now(utc);
        } else {
            latest = ZonedDateTime.parse(since);
        }
        return latest;
    }
}
