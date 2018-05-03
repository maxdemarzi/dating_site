package com.maxdemarzi.states;

import com.maxdemarzi.Exceptions;

class StateExceptions extends Exceptions {

    static final Exceptions stateNotFound = new Exceptions(400, "State not Found.");

    StateExceptions(int code, String error) {
       super(code, error);
   }
}