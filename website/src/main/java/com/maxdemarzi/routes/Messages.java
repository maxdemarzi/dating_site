package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.*;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class Messages  extends Jooby {
    public Messages() {
        super("messages");
    }
    {
        get("/messages", req -> {
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            User authenticated = App.getUserProfile(requested_by);

            Response<User> userResponse = App.api.getProfile(requested_by, null).execute();
            if (userResponse.isSuccessful()) {
                User user = userResponse.body();
                Integer limit = req.param("limit").intValue(25);
                Integer offset = req.param("offset").intValue(0);

                Response<List<Conversation>> conversationResponse = App.api.getConversations(user.getUsername()).execute();
                List<Conversation> conversations = new ArrayList<>();
                if (conversationResponse.isSuccessful()) {
                    conversations = conversationResponse.body();
                }

                return views.messages.template(authenticated, conversations);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });

        get("/messages/{username}", req -> {
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            User authenticated = App.getUserProfile(requested_by);

            Response<User> userResponse = App.api.getProfile(requested_by, null).execute();
            Response<User> user2Response = App.api.getProfile(req.param("username").value(), null).execute();
            if (userResponse.isSuccessful() && user2Response.isSuccessful()) {
                User user = userResponse.body();
                User user2 = user2Response.body();

                Integer limit = req.param("limit").intValue(25);
                Integer offset = req.param("offset").intValue(0);

                Response<List<Message>> conversationResponse = App.api.getConversation(user.getUsername(), user2.getUsername()).execute();
                List<Message> messages = new ArrayList<>();
                if (conversationResponse.isSuccessful()) {
                    messages = conversationResponse.body();
                }

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
