package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Post;
import com.maxdemarzi.models.User;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.util.List;

public class Fives extends Jooby {
    public Fives() {
        super("fives");
    }

    {
        get("/high_fives", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = App.getUserProfile(username);

            Response<List<Post>> response = App.api.getHighFives(username).execute();
            List<Post> posts;
            if (response.isSuccessful()) {
                posts = response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }

            return views.fives.template(authenticated, posts, "high");
        });

        get("/low_fives", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = App.getUserProfile(username);

            Response<List<Post>> response = App.api.getLowFives(username).execute();
            List<Post> posts;
            if (response.isSuccessful()) {
                posts = response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }

            return views.fives.template(authenticated, posts, "low");
        });

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
