package com.maxdemarzi.attributes;

import com.maxdemarzi.has.Has;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.users.Users;
import com.maxdemarzi.wants.Wants;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static com.maxdemarzi.schema.Properties.*;
import static com.maxdemarzi.users.Users.findUser;
import static java.util.Collections.reverseOrder;

@Path("/attributes")
public class Attributes {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Comparator<Map<String, Object>> pickedComparator = Comparator.comparing(m -> (Boolean)m.get(WANT) || (Boolean)m.get(HAVE));
    private static final Comparator<Map<String, Object>> popularComparator = Comparator.comparing(m -> (Long) m.get(WANTS) + (Long)m.get(HAS), reverseOrder());


    @GET
    public Response getAttributes(@QueryParam("username") final String username,
                                 @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                 @QueryParam("offset") @DefaultValue("0") final Integer offset,
                                 @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {

            HashSet<Node> userHas = new HashSet<>();
            HashSet<Node> userWants = new HashSet<>();
            if (username != null) {
                Node user = Users.findUser(username, db);
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                    userHas.add(r1.getEndNode());
                }
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                    userWants.add(r1.getEndNode());
                }
            }
            ResourceIterator<Node> attributes = db.findNodes(Labels.Attribute);
            while (attributes.hasNext()) {
                Node attribute = attributes.next();
                Map<String, Object> properties = attribute.getAllProperties();
                properties.put(HAVE, userHas.contains(attribute));
                properties.put(WANT, userWants.contains(attribute));
                properties.put(WANTS, attribute.getDegree(RelationshipTypes.WANTS, Direction.INCOMING));
                properties.put(HAS, attribute.getDegree(RelationshipTypes.HAS, Direction.INCOMING));
                results.add(properties);
            }
            tx.success();
        }
        results.sort(pickedComparator.thenComparing(popularComparator));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(Math.min(results.size(), offset), Math.min(results.size(), limit))))
                .build();
    }

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
