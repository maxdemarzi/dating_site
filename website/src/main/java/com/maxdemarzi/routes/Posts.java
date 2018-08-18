package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Post;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Posts extends Jooby {
    public Posts() {
        super("posts");
    }

    {
        post("/post", req -> {
            Post post = req.form(Post.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();

            Response<Post> response = App.api.createPost(username, post).execute();
            if (response.isSuccessful()) {
                return Results.redirect("/home");
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
