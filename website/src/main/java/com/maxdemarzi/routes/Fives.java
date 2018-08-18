package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Attribute;
import com.maxdemarzi.models.Post;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Fives extends Jooby {
    public Fives() {
        super("fives");
    }

    {

        post("/five", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Post> response;
            if (req.param("high_five_button").isSet()) {
                response = App.api.createHighFive(username, req.param("username2").value(), req.param("time").value()).execute();
            } else {
                response = App.api.createLowFive(username, req.param("username2").value(), req.param("time").value()).execute();
            }
            if (response.isSuccessful()) {
                return Results.redirect(req.header("Referer").value());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        }).produces("json");;

    }

}
