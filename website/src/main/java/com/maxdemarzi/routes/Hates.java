package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Thing;
import com.maxdemarzi.models.User;
import org.jooby.*;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class Hates extends Jooby {
    {
        get("/user/{username}/hates", req -> {
            API api = require(API.class);
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            User authenticated = api.getUserProfile(requested_by);

            Response<User> userResponse = api.getProfile(req.param("username").value(), requested_by).execute();
            if (userResponse.isSuccessful()) {
                User user = userResponse.body();
                Integer limit = req.param("limit").intValue(25);
                Integer offset = req.param("offset").intValue(0);

                Response<List<Thing>> thingsResponse = api.getHates(user.getUsername(), limit, offset, requested_by).execute();
                List<Thing> things = new ArrayList<>();
                if (thingsResponse.isSuccessful()) {
                    things = thingsResponse.body();
                }

                return views.things.template(authenticated, user, things);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
