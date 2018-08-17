package com.maxdemarzi.fives;

import com.maxdemarzi.Exceptions;

public class FiveExceptions extends Exceptions {

    static final Exceptions alreadyHighFivedPost = new Exceptions(400, "Already high fived Post.");
    static final Exceptions alreadyLowFivedPost = new Exceptions(400, "Already low fived Post.");

    static final Exceptions overHighFiveLimit = new Exceptions(403, "Over high five Limit.");
    static final Exceptions overLowFiveLimit = new Exceptions(403, "Over low five Limit.");

    FiveExceptions(int code, String error) {
        super(code, error);
    }
}
