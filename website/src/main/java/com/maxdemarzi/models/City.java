package com.maxdemarzi.models;

import lombok.Data;

@Data
public class City {
    private Long ID;
    private String name;
    private String full_name;
    private String geoname_id;
    private Integer live_in;
}
