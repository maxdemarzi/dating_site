package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Attribute;
import com.maxdemarzi.models.User;
import org.jooby.*;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class Wants extends Jooby {
    {
        get("/user/{username}/wants", req -> {
            API api = require(API.class);
            String requested_by = req.get("requested_by");
            if (requested_by.equals("anonymous")) requested_by = null;
            User authenticated = api.getUserProfile(requested_by);

            Response<User> userResponse = api.getProfile(req.param("username").value(), requested_by).execute();
            if (userResponse.isSuccessful()) {
                User user = userResponse.body();
                Integer limit = req.param("limit").intValue(25);
                Integer offset = req.param("offset").intValue(0);

                Response<List<Attribute>> attributesResponse = api.getWants(user.getUsername(), limit, offset, requested_by).execute();
                List<Attribute> attributes = new ArrayList<>();
                if (attributesResponse.isSuccessful()) {
                    attributes = attributesResponse.body();
                }

                return views.attributes.template(authenticated, user, attributes);
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
