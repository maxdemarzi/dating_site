package com.maxdemarzi.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class User {
    private String username;
    private String name;
    private String email;
    private String password;
    private String hash;
    private Long time;
    private Integer likes;
    private Integer hates;
    private Integer posts;
    private Integer low_fives;
    private Integer high_fives;

    private ArrayList<HashMap<String, Object>> wants;
    private ArrayList<HashMap<String, Object>> has;
}