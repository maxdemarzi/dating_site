package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.*;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.util.List;

public class Messages  extends Jooby {
    {
        get("/messages", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = api.getUserProfile(username);

            Integer limit = req.param("limit").intValue(100);

            Response<List<Conversation>> conversationResponse = api.getConversations(username, limit).execute();
            if (conversationResponse.isSuccessful()) {
                List<Conversation> conversations = conversationResponse.body();
                return views.messages.template(authenticated, conversations);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });

        get("/messages/{username}", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = api.getUserProfile(username);
            User user2 = api.getUserProfile(req.param("username").value());

            Integer limit = req.param("limit").intValue(25);
            Integer offset = req.param("offset").intValue(0);

            Response<List<Message>> conversationResponse = api.getConversation(username, req.param("username").value()).execute();
            if (conversationResponse.isSuccessful()) {
                List<Message> messages = conversationResponse.body();
                return views.conversation.template(authenticated, user2, messages);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });

        post("/messages/{username2}", req -> {
            API api = require(API.class);
            Message message = req.form(Message.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Message> response = api.createMessage(username, req.param("username2").value(), message).execute();
            if (response.isSuccessful()) {
                return Results.redirect("/messages/" + req.param("username2").value());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
