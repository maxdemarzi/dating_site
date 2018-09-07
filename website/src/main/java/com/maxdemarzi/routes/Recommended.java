package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.User;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.util.List;

public class Recommended extends Jooby {
    {
        get("/recommended", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            com.maxdemarzi.models.User authenticated = api.getUserProfile(username);

            Response<List<User>> recommendedResponse = api.getRecommended(username).execute();
            if (recommendedResponse.isSuccessful()) {
                List<User> users = recommendedResponse.body();
                return views.users.template(authenticated, users);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
