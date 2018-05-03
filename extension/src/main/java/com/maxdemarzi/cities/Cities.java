package com.maxdemarzi.cities;

import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.schema.Properties.GEONAME_ID;
import static com.maxdemarzi.schema.Properties.LIVE_IN;
import static com.maxdemarzi.schema.Properties.NAME;
import static org.neo4j.helpers.collection.MapUtil.map;

@Path("/cities")
public class Cities {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Node findCity(String geoname_id, @Context GraphDatabaseService db) {
        if (geoname_id == null) { return null; }
        Node city = db.findNode(Labels.City, GEONAME_ID, geoname_id);
        if (city == null) { throw CityExceptions.cityNotFound; }
        return city;
    }

    public static Node findCity(String name, String state, @Context GraphDatabaseService db) {
            ResourceIterator<Node> cities = db.findNodes(Labels.City, NAME, name);
            while (cities.hasNext()) {
                Node city = cities.next();
                for (Relationship inLocation : city.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_LOCATION)) {
                    Node location = inLocation.getEndNode();
                    if (location.getProperty(NAME).equals(state)) {
                        return city;
                    }
                }
            }
        throw CityExceptions.cityNotFound;
    }

    public static HashSet<Node> findCitiesNearby(Node city, Integer distance, @Context GraphDatabaseService db) {
        HashSet<Node> cities = new HashSet<>();
        Result executionResult = db.execute(
             "MATCH (c:City), (c2:City)" +
                "WHERE ID(c) = $city " +
                "  AND distance(c.location, c2.location) <= $distance " +
                "RETURN c2", map("city", city.getId(), "distance", distance));
        ResourceIterator<Node> resultIterator = executionResult.columnAs( "c2" );
        while(resultIterator.hasNext()) {
            cities.add(resultIterator.next());
        }

        return cities;
    }

    @GET
    @Path("/{geoname_id}")
    public Response getCity(@PathParam("geoname_id") final String geoname_id,
                             @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> properties;
        try (Transaction tx = db.beginTx()) {
            Node city = findCity(geoname_id, db);
            properties = city.getAllProperties();
            properties.put(LIVE_IN, city.getDegree(RelationshipTypes.IN_LOCATION, Direction.INCOMING));
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(properties)).build();
    }

    @GET
    @Path("/{name}/{state}")
    public Response getCity(@PathParam("name") final String name,
                            @PathParam("state") final String state,
                            @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> properties;
        try (Transaction tx = db.beginTx()) {

            ResourceIterator<Node> cities = db.findNodes(Labels.City, NAME, name);
            while (cities.hasNext()) {
                Node city = cities.next();
                for (Relationship inLocation : city.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_LOCATION)) {
                    Node location = inLocation.getEndNode();
                    if (location.getProperty(NAME).equals(state)) {
                        properties = city.getAllProperties();
                        properties.put(LIVE_IN, city.getDegree(RelationshipTypes.IN_LOCATION, Direction.INCOMING));
                        return Response.ok().entity(objectMapper.writeValueAsString(properties)).build();
                    }
                }
            }
            tx.success();
        }

        throw CityExceptions.cityNotFound;
    }

}
