package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Post;
import com.maxdemarzi.models.User;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.util.List;

public class Fives extends Jooby {
    {
        get("/high_fives", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = api.getUserProfile(username);

            Response<List<Post>> response = api.getHighFives(username).execute();
            List<Post> posts;
            if (response.isSuccessful()) {
                posts = response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }

            return views.fives.template(authenticated, posts, "high");
        });

        get("/low_fives", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            User authenticated = api.getUserProfile(username);

            Response<List<Post>> response = api.getLowFives(username).execute();
            List<Post> posts;
            if (response.isSuccessful()) {
                posts = response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }

            return views.fives.template(authenticated, posts, "low");
        });

        post("/high_five", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Post> response = api.createHighFive(username, req.param("username2").value(), req.param("id").longValue()).execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });

        post("/low_five", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Post> response = api.createLowFive(username, req.param("username2").value(), req.param("id").longValue()).execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }

}
