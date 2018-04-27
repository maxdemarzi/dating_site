package com.maxdemarzi.models;

import humanize.Humanize;
import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private String status;
    private String name;
    private String username;
    private String hash;
    private Long time;
    private String human_time;
    private Integer low_fives;
    private boolean low_fived;
    private Integer high_fives;
    private boolean high_fived;

    public String humanTime() {
        return Humanize.naturalTime(new Date(time * 1000));
    }
}
