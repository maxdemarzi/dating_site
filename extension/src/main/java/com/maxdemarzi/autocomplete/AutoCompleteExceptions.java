package com.maxdemarzi.autocomplete;

import com.maxdemarzi.Exceptions;

public class AutoCompleteExceptions extends Exceptions {
    public static final Exceptions labelNotValid = new Exceptions(400, "Label not Valid.");
    public static final Exceptions propertyNotValid = new Exceptions(400, "Property not Valid.");

    private AutoCompleteExceptions(int code, String error) {
        super(code, error);
    }
}
