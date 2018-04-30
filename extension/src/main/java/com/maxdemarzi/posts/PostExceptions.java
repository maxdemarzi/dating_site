package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;

 class PostExceptions extends Exceptions {

     static final Exceptions missingStatusParameter = new Exceptions(400, "Missing status Parameter.");
     static final Exceptions emptyStatusParameter = new Exceptions(400, "Empty status Parameter.");
     static final Exceptions postNotFound = new Exceptions(400, "Post not Found.");

     PostExceptions(int code, String error) {
        super(code, error);
    }
}
