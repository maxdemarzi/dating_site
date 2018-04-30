package com.maxdemarzi.tags;

import com.maxdemarzi.Exceptions;

 class TagExceptions extends Exceptions {

     static final Exceptions tagNotFound = new Exceptions(400, "Tag Not Found.");

     TagExceptions(int code, String error) {
        super(code, error);
    }
}