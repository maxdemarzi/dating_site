package com.maxdemarzi.likes;

import com.maxdemarzi.Exceptions;

public class LikeExceptions extends Exceptions {

    public static final Exceptions alreadyLikesThing = new Exceptions(400, "Already likes Thing.");
    public static final Exceptions notLikingThing = new Exceptions(400, "Not liking Thing.");

    private LikeExceptions(int code, String error) {
        super(code, error);
    }
}