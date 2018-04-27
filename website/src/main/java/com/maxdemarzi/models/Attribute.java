package com.maxdemarzi.models;

import lombok.Data;

@Data
public class Attribute {
    private String name;
    private Integer wants;
    private Integer has;
    private boolean want;
    private boolean have;
}