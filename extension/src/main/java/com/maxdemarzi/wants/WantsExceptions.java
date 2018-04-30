package com.maxdemarzi.wants;

import com.maxdemarzi.Exceptions;

 class WantsExceptions extends Exceptions {

     static final Exceptions alreadyWantsAttribute = new Exceptions(400, "Already wants Attribute.");
     static final Exceptions notWantingAttribute = new Exceptions(400, "Not wanting Attribute.");

     WantsExceptions(int code, String error) {
        super(code, error);
    }
}