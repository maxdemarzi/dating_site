package com.maxdemarzi.countries;

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

@Path("/countries")
public class Countries {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    public Response getCountries(@Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> countries = db.findNodes(Labels.Country);
            while (countries.hasNext()) {
                Node country = countries.next();
                results.add(country.getAllProperties());
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/{code}/states")
    public Response getStates(@PathParam("code") final String code,
                              @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> states = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node country = findCountry(code, db);
            for (Relationship inLocation : country.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                Node state = inLocation.getStartNode();
                if (state.hasLabel(Labels.State)) {
                    states.add(state.getAllProperties());
                }
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(states)).build();
    }
    private static Node findCountry(String code, @Context GraphDatabaseService db) {
        Node country = db.findNode(Labels.Country, CODE, code);
        if (country != null) return country;

        throw CountryExceptions.countryNotFound;
    }
}
