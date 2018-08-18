package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Attribute;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Attributes extends Jooby {
    public Attributes() {
        super("attributes");
    }

    {
        post("/attribute", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<Attribute> response;
            if (req.param("have_button").isSet()) {
                response = App.api.createHas(username, req.param("attribute").value()).execute();
            } else {
                response = App.api.createWants(username, req.param("attribute").value()).execute();
            }
            if (response.isSuccessful()) {
                return Results.redirect(req.header("Referer").value());
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        });
    }
}
