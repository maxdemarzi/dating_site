package com.maxdemarzi.conversations;

import com.maxdemarzi.Exceptions;

public class ConversationExceptions extends Exceptions {

    static final Exceptions conversationNotFound = new Exceptions(400, "Conversation not Found.");
    static final Exceptions conversationNotAllowed = new Exceptions(403, "Conversation not Allowed.");

    ConversationExceptions(int code, String error) {
        super(code, error);
    }
}