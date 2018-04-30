package com.maxdemarzi.blocks;

import com.maxdemarzi.Exceptions;

class BlockExceptions extends Exceptions {

    static final Exceptions alreadyBlockingUser = new Exceptions(400, "Already blocking User.");
    static final Exceptions notBlockingUser = new Exceptions(400, "Not blocking User.");

    BlockExceptions(int code, String error) {
        super(code, error);
    }
}
