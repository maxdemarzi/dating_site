package com.maxdemarzi.routes;

import com.maxdemarzi.App;
import com.maxdemarzi.models.Attribute;
import com.maxdemarzi.models.City;
import com.maxdemarzi.models.Thing;
import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Status;
import retrofit2.Response;

import java.util.List;


public class AutoCompletes extends Jooby {
    public AutoCompletes() {
        super("autocomplete");
    }

    {
        get("/autocomplete/city/{query}", req -> {
            Response<List<City>> cityResponse = App.api.autoCompleteCity(req.param("query").value().toLowerCase(), "full_name").execute();
            if (cityResponse.isSuccessful()) {
                return cityResponse.body();
            }
            throw new Err(Status.CONFLICT, "There was a problem autocompleting the city");
        }).produces("json");

        get("/autocomplete/attribute/{query}", req -> {
            Response<List<Attribute>> attributeResponse = App.api.autoCompleteAttribute(req.param("query").value().toLowerCase(), "name").execute();
            if (attributeResponse.isSuccessful()) {
                return attributeResponse.body();
            }
            throw new Err(Status.CONFLICT, "There was a problem autocompleting the attribute");
        }).produces("json");


        get("/autocomplete/thing/{query}", req -> {
            Response<List<Thing>> thingResponse = App.api.autoCompleteThing(req.param("query").value().toLowerCase(), "name").execute();
            if (thingResponse.isSuccessful()) {
                return thingResponse.body();
            }
            throw new Err(Status.CONFLICT, "There was a problem autocompleting the thing");
        }).produces("json");

    }
}
