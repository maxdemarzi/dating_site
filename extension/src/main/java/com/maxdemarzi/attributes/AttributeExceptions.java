package com.maxdemarzi.attributes;

import com.maxdemarzi.Exceptions;

public class AttributeExceptions extends Exceptions {

    public static final Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
    public static final Exceptions emptyNameParameter = new Exceptions(400, "Empty name Parameter.");

    public static final Exceptions attributeNotFound = new Exceptions(400, "Attribute not Found.");

    private AttributeExceptions(int code, String error) {
        super(code, error);
    }
}