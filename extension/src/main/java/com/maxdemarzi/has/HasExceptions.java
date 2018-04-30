package com.maxdemarzi.has;

import com.maxdemarzi.Exceptions;

 class HasExceptions extends Exceptions {

     static final Exceptions alreadyHasAttribute = new Exceptions(400, "Already has Attribute.");
     static final Exceptions notHavingAttribute = new Exceptions(400, "Not having Attribute.");

     HasExceptions(int code, String error) {
        super(code, error);
    }
}