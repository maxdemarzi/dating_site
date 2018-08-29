package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.models.*;
import org.jooby.*;
import retrofit2.Response;

import java.util.List;

public class AutoCompletes extends Jooby {
    {
        get("/autocomplete/city/{query}", req -> {
            API api = require(API.class);
            Response<List<City>> cityResponse = api.autoCompleteCity(req.param("query").value().toLowerCase(), "full_name").execute();
            if (cityResponse.isSuccessful()) {
                return cityResponse.body();
            }
            throw new Err(Status.CONFLICT, "There was a problem autocompleting the city");
        });

        get("/autocomplete/attribute/{query}", req -> {
            API api = require(API.class);
            Response<List<Attribute>> attributeResponse = api.autoCompleteAttribute(req.param("query").value().toLowerCase(), "name").execute();
            if (attributeResponse.isSuccessful()) {
                return attributeResponse.body();
            }
            throw new Err(Status.CONFLICT, "There was a problem autocompleting the attribute");
        });


        get("/autocomplete/thing/{query}", req -> {
            API api = require(API.class);
            Response<List<Thing>> thingResponse = api.autoCompleteThing(req.param("query").value().toLowerCase(), "name").execute();
            if (thingResponse.isSuccessful()) {
                return thingResponse.body();
            }
            throw new Err(Status.CONFLICT, "There was a problem autocompleting the thing");
        });
    }
}
