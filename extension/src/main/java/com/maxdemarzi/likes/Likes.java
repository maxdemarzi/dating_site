package com.maxdemarzi.likes;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.hates.Hates;
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

@Path("/users/{username}/likes")
public class Likes {

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> sharedComparator = Comparator.comparing(m -> (Boolean)m.get(SHARED));
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    public Response getLikes(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("offset") @DefaultValue("0")  final Integer offset,
                             @QueryParam("username2") final String username2,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = null;
            HashSet<Node> user2Likes = new HashSet<>();
            if (username2 != null) {
                user2 = Users.findUser(username2, db);
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                    user2Likes.add(r1.getEndNode());
                }
            }
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                Node thing = r1.getEndNode();
                Map<String, Object> properties = thing.getAllProperties();
                properties.put(TIME, r1.getProperty("time"));
                properties.put(SHARED, user2Likes.contains(thing));
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

            if (userLikesThing(user, thing)) {
                throw LikeExceptions.alreadyLikesThing;
            }

            Relationship like = user.createRelationshipTo(thing, RelationshipTypes.LIKES);
            like.setProperty(TIME, ZonedDateTime.now(utc));
            results = thing.getAllProperties();
            results.put(LIKED, true);
            results.put(HATED, Hates.userHatesThing(user, thing));
            results.put(LIKES, thing.getDegree(RelationshipTypes.LIKES, Direction.INCOMING));
            results.put(HATES, thing.getDegree(RelationshipTypes.HATES, Direction.INCOMING));
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @DELETE
    @Path("/{name}")
    public Response removeLike(@PathParam("username") final String username,
                               @PathParam("name") final String name,
                               @Context GraphDatabaseService db) throws IOException {
        boolean liked = false;
        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node thing = Things.findThing(name, db);

            if (user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING)
                    < thing.getDegree(RelationshipTypes.LIKES, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                    if (r1.getEndNode().equals(thing)) {
                        r1.delete();
                        liked = true;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : thing.getRelationships(Direction.INCOMING, RelationshipTypes.LIKES)) {
                    if (r1.getStartNode().equals(user)) {
                        r1.delete();
                        liked = true;
                        break;
                    }
                }
            }
            tx.success();
        }

        if(!liked) {
            throw LikeExceptions.notLikingThing;
        }

        return Response.noContent().build();
    }

    public static boolean userLikesThing(Node user, Node thing) {
        if (user == null) {
            return false;
        }

        boolean alreadyLiked = false;
        if (user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING)
                < thing.getDegree(RelationshipTypes.LIKES, Direction.INCOMING) ) {
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                if (r1.getEndNode().equals(thing)) {
                    alreadyLiked = true;
                    break;
                }
            }
        } else {
            for (Relationship r1 : thing.getRelationships(Direction.INCOMING, RelationshipTypes.LIKES)) {
                if (r1.getStartNode().equals(user)) {
                    alreadyLiked = true;
                    break;
                }
            }
        }
        return alreadyLiked;
    }
}
