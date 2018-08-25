package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.*;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.util.List;

public class Messages  extends Jooby {
    public Messages() {
        super("messages");
    }
    {
        get("/messages", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = App.getUserProfile(username);

            Integer limit = req.param("limit").intValue(25);
            Integer offset = req.param("offset").intValue(0);

            Response<List<Conversation>> conversationResponse = App.api.getConversations(username).execute();
            if (conversationResponse.isSuccessful()) {
                List<Conversation> conversations = conversationResponse.body();
                return views.messages.template(authenticated, conversations);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });

        get("/messages/{username}", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = App.getUserProfile(username);
            User user2 = App.getUserProfile(req.param("username").value());

            Integer limit = req.param("limit").intValue(25);
            Integer offset = req.param("offset").intValue(0);

            Response<List<Message>> conversationResponse = App.api.getConversation(username, req.param("username").value()).execute();
            if (conversationResponse.isSuccessful()) {
                List<Message> messages = conversationResponse.body();
                return views.conversation.template(authenticated, user2, messages);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });

        post("/messages/{username2}", req -> {
            Message message = req.form(Message.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Message> response = App.api.createMessage(username, req.param("username2").value(), message).execute();
            if (response.isSuccessful()) {
                return Results.redirect("/messages/" + req.param("username2").value());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
