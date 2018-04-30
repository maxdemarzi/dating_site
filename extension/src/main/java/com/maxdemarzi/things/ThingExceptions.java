package com.maxdemarzi.things;

import com.maxdemarzi.Exceptions;

 class ThingExceptions extends Exceptions {

     static final Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
     static final Exceptions emptyNameParameter = new Exceptions(400, "Empty name Parameter.");

     static final Exceptions thingNotFound = new Exceptions(400, "Thing not Found.");

     ThingExceptions(int code, String error) {
        super(code, error);
    }
}