package com.maxdemarzi.things;

import com.maxdemarzi.Exceptions;

public class ThingExceptions extends Exceptions {

    public static final Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
    public static final Exceptions emptyNameParameter = new Exceptions(400, "Empty name Parameter.");

    public static final Exceptions thingNotFound = new Exceptions(400, "Thing not Found.");

    private ThingExceptions(int code, String error) {
        super(code, error);
    }
}