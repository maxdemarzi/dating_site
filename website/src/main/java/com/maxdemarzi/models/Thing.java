package com.maxdemarzi.models;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class Thing {
    private Long id;
    private String name;
    private String lowercase_name;
    private Long time;
    private Integer likes;
    private Integer hates;
    private Boolean liked;
    private Boolean hated;
    private Boolean shared;

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

    public String when() {
        Date date = new Date(time * 1000);
        return dateFormat.format(date);
    }

}
