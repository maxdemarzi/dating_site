package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Post;
import org.jooby.*;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import static com.maxdemarzi.App.getUserProfile;

public class Users extends Jooby {
    public Users() {
        super("user");
    }
    {
        get("/user/{username}", req -> {
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            com.maxdemarzi.models.User authenticated = getUserProfile(requested_by);

            Response<com.maxdemarzi.models.User> userResponse = App.api.getProfile(req.param("username").value(), requested_by).execute();
            if (userResponse.isSuccessful()) {
                com.maxdemarzi.models.User user = userResponse.body();

                Response<List<Post>> postsResponse = App.api.getPosts(req.param("username").value()).execute();
                List<Post> posts = new ArrayList<>();
                if (postsResponse.isSuccessful()) {
                    posts = postsResponse.body();
                }

                return views.user.template(authenticated, user, posts, App.getTags());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
