package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.Attribute;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Attributes extends Jooby {

    {
        post("/attribute", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Attribute> response;
            if (req.param("have_button").isSet()) {
                response = api.createHas(username, req.param("attribute").value()).execute();
            } else {
                response = api.createWants(username, req.param("attribute").value()).execute();
            }
            if (response.isSuccessful()) {
                return Results.redirect(req.header("Referer").value());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
