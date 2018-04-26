package com.maxdemarzi.has;

import com.maxdemarzi.Exceptions;

public class HasExceptions extends Exceptions {

    public static final Exceptions alreadyHasAttribute = new Exceptions(400, "Already has Attribute.");
    public static final Exceptions notHavingAttribute = new Exceptions(400, "Not having Attribute.");

    private HasExceptions(int code, String error) {
        super(code, error);
    }
}