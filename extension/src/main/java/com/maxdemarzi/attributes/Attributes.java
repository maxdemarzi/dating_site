package com.maxdemarzi.attributes;

import com.maxdemarzi.has.Has;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.wants.Wants;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static com.maxdemarzi.schema.Properties.HAVE;
import static com.maxdemarzi.schema.Properties.HAS;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.WANT;
import static com.maxdemarzi.schema.Properties.WANTS;
import static com.maxdemarzi.users.Users.findUser;

@Path("/attributes")
public class Attributes {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/{name}")
    public Response getAttribute(@PathParam("name") final String name,
                               @QueryParam("username") final String username,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node attribute = findAttribute(name, db);
            results = attribute.getAllProperties();

            if (username != null) {
                Node user = findUser(username, db);
                results.put(HAVE, Has.userHasAttribute(user, attribute));
                results.put(WANT, Wants.userWantsAttribute(user, attribute));
            }

            results.put(WANTS, attribute.getDegree(RelationshipTypes.WANTS, Direction.INCOMING));
            results.put(HAS, attribute.getDegree(RelationshipTypes.HAS, Direction.INCOMING));

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }


    public static Node findAttribute(String name, @Context GraphDatabaseService db) {
        if (name == null) { return null; }
        Node attribute = db.findNode(Labels.Attribute, NAME, name);
        if (attribute == null) { throw AttributeExceptions.attributeNotFound; }
        return attribute;
    }
}
