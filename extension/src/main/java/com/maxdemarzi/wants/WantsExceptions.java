package com.maxdemarzi.wants;

import com.maxdemarzi.Exceptions;

public class WantsExceptions extends Exceptions {

    public static final Exceptions alreadyWantsAttribute = new Exceptions(400, "Already want Attribute.");
    public static final Exceptions notWantingAttribute = new Exceptions(400, "Not wanting Attribute.");

    private WantsExceptions(int code, String error) {
        super(code, error);
    }
}