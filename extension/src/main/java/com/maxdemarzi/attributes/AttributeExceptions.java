package com.maxdemarzi.attributes;

import com.maxdemarzi.Exceptions;

 class AttributeExceptions extends Exceptions {

     static final Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
     static final Exceptions emptyNameParameter = new Exceptions(400, "Empty name Parameter.");

     static final Exceptions attributeNotFound = new Exceptions(400, "Attribute not Found.");

    AttributeExceptions(int code, String error) {
        super(code, error);
    }
}