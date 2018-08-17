package com.maxdemarzi.hates;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.likes.Likes;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.things.Things;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.HATED;
import static com.maxdemarzi.schema.Properties.HATES;
import static com.maxdemarzi.schema.Properties.LIKED;
import static com.maxdemarzi.schema.Properties.LIKES;
import static com.maxdemarzi.schema.Properties.SHARED;
import static com.maxdemarzi.schema.Properties.TIME;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/hates")
public class Hates {

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> sharedComparator = Comparator.comparing(m -> (Boolean)m.get(SHARED));
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    public Response getHates(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("offset") @DefaultValue("0")  final Integer offset,
                             @QueryParam("username2") final String username2,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = null;
            HashSet<Node> user2Hates = new HashSet<>();
            if (username2 != null) {
                user2 = Users.findUser(username2, db);
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.HATES)) {
                    user2Hates.add(r1.getEndNode());
                }
            }
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HATES)) {
                Node thing = r1.getEndNode();
                Map<String, Object> properties = thing.getAllProperties();
                properties.put(TIME, r1.getProperty("time"));
                properties.put(SHARED, user2Hates.contains(thing));
                properties.put(LIKED, Likes.userLikesThing(user2, thing));
                properties.put(HATED, Hates.userHatesThing(user2, thing));
                properties.put(LIKES, thing.getDegree(RelationshipTypes.LIKES, Direction.INCOMING));
                properties.put(HATES, thing.getDegree(RelationshipTypes.HATES, Direction.INCOMING));
                results.add(properties);
            }
            tx.success();
        }

        results.sort(sharedComparator.thenComparing(timedComparator));

        if (offset > results.size()) {
            return Response.ok().entity(objectMapper.writeValueAsString(
                    results.subList(0, 0)))
                    .build();
        } else {
            return Response.ok().entity(objectMapper.writeValueAsString(
                    results.subList(offset, Math.min(results.size(), limit + offset))))
                    .build();
        }
    }

    @POST
    @Path("/{name}")
    public Response createLike(@PathParam("username") final String username,
                               @PathParam("name") final String name,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node thing = Things.findThing(name, db);

            if (userHatesThing(user, thing)) {
                throw HateExceptions.alreadyHatesThing;
            }

            Relationship hate = user.createRelationshipTo(thing, RelationshipTypes.HATES);
            hate.setProperty(TIME, ZonedDateTime.now(utc));
            results = thing.getAllProperties();
            results.put(HATED, true);
            results.put(LIKED, Likes.userLikesThing(user, thing));
            results.put(LIKES, thing.getDegree(RelationshipTypes.LIKES, Direction.INCOMING));
            results.put(HATES, thing.getDegree(RelationshipTypes.HATES, Direction.INCOMING));
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @DELETE
    @Path("/{name}")
    public Response removeHate(@PathParam("username") final String username,
                               @PathParam("name") final String name,
                               @Context GraphDatabaseService db) throws IOException {
        boolean hated = false;
        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node thing = Things.findThing(name, db);

            if (user.getDegree(RelationshipTypes.HATES, Direction.OUTGOING)
                    < thing.getDegree(RelationshipTypes.HATES, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HATES)) {
                    if (r1.getEndNode().equals(thing)) {
                        r1.delete();
                        hated = true;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : thing.getRelationships(Direction.INCOMING, RelationshipTypes.HATES)) {
                    if (r1.getStartNode().equals(user)) {
                        r1.delete();
                        hated = true;
                        break;
                    }
                }
            }
            tx.success();
        }

        if(!hated) {
            throw HateExceptions.notHatingThing;
        }

        return Response.noContent().build();
    }

    public static boolean userHatesThing(Node user, Node thing) {
        if (user == null) {
            return false;
        }

        boolean alreadyHated = false;
        if (user.getDegree(RelationshipTypes.HATES, Direction.OUTGOING)
                < thing.getDegree(RelationshipTypes.HATES, Direction.INCOMING) ) {
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HATES)) {
                if (r1.getEndNode().equals(thing)) {
                    alreadyHated = true;
                    break;
                }
            }
        } else {
            for (Relationship r1 : thing.getRelationships(Direction.INCOMING, RelationshipTypes.HATES)) {
                if (r1.getStartNode().equals(user)) {
                    alreadyHated = true;
                    break;
                }
            }
        }
        return alreadyHated;
    }
}
