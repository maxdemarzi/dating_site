package com.maxdemarzi.models;

import lombok.Data;

@Data
public class Thing {
    private String name;
    private Integer likes;
    private Integer hates;
    private boolean liked;
    private boolean hated;
}
