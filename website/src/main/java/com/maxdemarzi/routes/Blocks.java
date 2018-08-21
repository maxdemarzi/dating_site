package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.User;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

public class Blocks extends Jooby {
    public Blocks() {
        super("blocks");
    }
    {
        post("block", req -> {
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();
            Response<User> response = App.api.createBlocks(username, req.param("username2").value()).execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        }).produces("json");

    }
}
