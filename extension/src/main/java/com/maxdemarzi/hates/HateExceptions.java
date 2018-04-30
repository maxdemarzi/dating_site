package com.maxdemarzi.hates;

import com.maxdemarzi.Exceptions;

 class HateExceptions extends Exceptions {

     static final Exceptions alreadyHatesThing = new Exceptions(400, "Already hates Thing.");
     static final Exceptions notHatingThing = new Exceptions(400, "Not hating Thing.");

     HateExceptions(int code, String error) {
        super(code, error);
    }
}