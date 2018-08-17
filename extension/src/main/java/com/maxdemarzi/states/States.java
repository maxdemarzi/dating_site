package com.maxdemarzi.states;

import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.maxdemarzi.schema.Properties.CODE;

@Path("/states")
public class States {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/{code}/cities")
    public Response getCities(@PathParam("code") final String code,
                              @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> cities = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node location = findState(code, db);
            for (Relationship inLocation : location.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                Node city = inLocation.getStartNode();
                if (city.hasLabel(Labels.City)) {
                    cities.add(city.getAllProperties());
                }
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(cities)).build();
    }

    private static Node findState(String code, @Context GraphDatabaseService db) {
        Node state = db.findNode(Labels.State, CODE, code);
        if (state != null) return state;

        throw StateExceptions.stateNotFound;
    }
}
