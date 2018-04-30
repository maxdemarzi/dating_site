package com.maxdemarzi.autocomplete;

import com.maxdemarzi.Exceptions;

 class AutoCompleteExceptions extends Exceptions {

     static final Exceptions labelNotValid = new Exceptions(400, "Label not Valid.");
     static final Exceptions propertyNotValid = new Exceptions(400, "Property not Valid.");

    AutoCompleteExceptions(int code, String error) {
        super(code, error);
    }
}
