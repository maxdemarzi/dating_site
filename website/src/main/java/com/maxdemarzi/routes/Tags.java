package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Post;
import com.maxdemarzi.models.User;
import org.jooby.*;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class Tags extends Jooby {
    {
        get("/tag/{hashtag}", req -> {
            API api = require(API.class);
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            User authenticated = api.getUserProfile(requested_by);
            String tag = req.param("hashtag").value();

            Response<List<Post>> tagResponse = api.getTag(tag, requested_by).execute();
            List<Post> posts = new ArrayList<>();
            if (tagResponse.isSuccessful()) {
                posts = tagResponse.body();
            }
            return views.tag.template(authenticated, tag, posts, api.getTagList());
        });
    }
}