package com.maxdemarzi.things;

import com.maxdemarzi.hates.Hates;
import com.maxdemarzi.likes.Likes;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static com.maxdemarzi.schema.Properties.*;

@Path("/things")
public class Things {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Node findThing(String name, @Context GraphDatabaseService db) {
        if (name == null) { return null; }
        Node thing = db.findNode(Labels.Thing, NAME, name);
        if (thing == null) { throw ThingExceptions.thingNotFound; }
        return thing;
    }

    @GET
    @Path("/{name}")
    public Response getThing(@PathParam("name") final String name,
                            @QueryParam("username") final String username,
                            @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> properties;
        try (Transaction tx = db.beginTx()) {
            Node thing = db.findNode(Labels.Thing, NAME, name);
            if (thing == null) {
                throw ThingExceptions.thingNotFound;
            }

            properties = thing.getAllProperties();
            if (username != null) {
                Node user = Users.findUser(username, db);
                properties.put(HATED, Hates.userHatesThing(user, thing));
                properties.put(LIKED, Likes.userLikesThing(user, thing));
            }
            properties.put(LIKES, thing.getDegree(RelationshipTypes.LIKES, Direction.INCOMING));
            properties.put(HATES, thing.getDegree(RelationshipTypes.HATES, Direction.INCOMING));
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(properties)).build();
    }

}
