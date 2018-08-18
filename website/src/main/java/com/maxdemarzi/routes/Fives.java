package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Post;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Fives extends Jooby {
    public Fives() {
        super("fives");
    }

    {
        post("/high_five", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Post> response = App.api.createHighFive(username, req.param("username2").value(), req.param("id").longValue()).execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        }).produces("json");

        post("/low_five", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Post> response = App.api.createLowFive(username, req.param("username2").value(), req.param("id").longValue()).execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        }).produces("json");
    }

}
