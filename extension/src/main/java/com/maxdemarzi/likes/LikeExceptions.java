package com.maxdemarzi.likes;

import com.maxdemarzi.Exceptions;

 class LikeExceptions extends Exceptions {

     static final Exceptions alreadyLikesThing = new Exceptions(400, "Already likes Thing.");
     static final Exceptions notLikingThing = new Exceptions(400, "Not liking Thing.");

     LikeExceptions(int code, String error) {
        super(code, error);
    }
}