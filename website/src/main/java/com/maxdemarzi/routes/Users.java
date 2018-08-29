package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Post;
import org.jooby.*;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class Users extends Jooby {
    {
        get("/user/{username}", req -> {
            API api = require(API.class);
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            com.maxdemarzi.models.User authenticated = api.getUserProfile(requested_by);

            Response<com.maxdemarzi.models.User> userResponse = api.getProfile(req.param("username").value(), requested_by).execute();
            if (userResponse.isSuccessful()) {
                com.maxdemarzi.models.User user = userResponse.body();

                Response<List<Post>> postsResponse = api.getPosts(req.param("username").value()).execute();
                List<Post> posts = new ArrayList<>();
                if (postsResponse.isSuccessful()) {
                    posts = postsResponse.body();
                }

                return views.user.template(authenticated, user, posts, api.getTagList());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
