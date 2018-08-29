package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Thing;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Things extends Jooby {
    {
        post("/thing", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Thing> response;
            if (req.param("like_button").isSet()) {
                response = api.createLikes(username, req.param("attribute").value()).execute();
            } else {
                response = api.createHates(username, req.param("attribute").value()).execute();
            }
            if (response.isSuccessful()) {
                return Results.redirect(req.header("Referer").value());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
