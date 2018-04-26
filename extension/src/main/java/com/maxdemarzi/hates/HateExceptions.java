package com.maxdemarzi.hates;

import com.maxdemarzi.Exceptions;

public class HateExceptions extends Exceptions {

    public static final Exceptions alreadyHatesThing = new Exceptions(400, "Already hates Thing.");
    public static final Exceptions notHatingThing = new Exceptions(400, "Not hating Thing.");

    private HateExceptions(int code, String error) {
        super(code, error);
    }
}