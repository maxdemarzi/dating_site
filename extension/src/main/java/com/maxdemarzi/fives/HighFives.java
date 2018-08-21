package com.maxdemarzi.fives;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.posts.PostExceptions;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.function.Function;

import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.*;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/high_fives")
public class HighFives {
    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    public Response getFives(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final String since,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        ZonedDateTime latest;
        if (since == null) {
            latest = ZonedDateTime.now(utc);
        } else {
            latest = ZonedDateTime.parse(since);
        }

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Map userProperties = user.getAllProperties();

            HashSet<Node> blocked = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                blocked.add(r1.getEndNode());
            }

            // Get user's timezone
            ZoneId zoneId = ZoneId.of((String) user.getProperty(TIMEZONE));
            ZonedDateTime startOfFiveDays = ZonedDateTime.now(zoneId).with(LocalTime.MIN).minusDays(5);

            // How many high fives did their posts receive within the last 5 days?
            ArrayList<RelationshipType> types = new ArrayList<>();
            for (RelationshipType t : user.getRelationshipTypes()) {
                if (t.name().startsWith("POSTED_ON")) {
                    types.add(t);
                }
            }

            for (Relationship r1 : user.getRelationships(types.toArray(new RelationshipType[0]))) {
                Node post = r1.getEndNode();
                Map<String, Object> result = post.getAllProperties();
                for (Relationship r : post.getRelationships(RelationshipTypes.HIGH_FIVED, Direction.INCOMING)) {
                    ZonedDateTime when = (ZonedDateTime) r.getProperty(TIME);
                    if (when.isAfter(startOfFiveDays) && when.isBefore(latest)) {
                        Node user2 = r.getStartNode();
                        if (!blocked.contains(user2)) {
                            Map<String, Object> user2Properties = user2.getAllProperties();
                            result.put(ID, post.getId());
                            result.put(TIME, when);
                            result.put(USERNAME, username);
                            result.put(USERNAME2, user2Properties.get(USERNAME));
                            result.put(NAME, userProperties.get(NAME));
                            result.put(NAME2, user2Properties.get(NAME));
                            result.put(HASH, userProperties.get(HASH));
                            result.put(HASH2, user2Properties.get(HASH));
                            result.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED, Direction.INCOMING));
                            result.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED, Direction.INCOMING));

                            results.add(result);
                        }
                    }
                }
            }
            tx.success();
        }
        results.sort(timedComparator);

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @POST
    @Path("/{username2}/{postId}")
    public Response createFive(String body, @PathParam("username") final String username,
                               @PathParam("username2") final String username2,
                               @PathParam("postId") final Long postId,
                               @Context GraphDatabaseService db) throws IOException {

        Map<String, Object> results;
        ZonedDateTime dateTime = ZonedDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);

            // Get user's timezone
            ZoneId zoneId = ZoneId.of((String)user.getProperty(TIMEZONE));
            ZonedDateTime startOfDay = ZonedDateTime.now(zoneId).with(LocalTime.MIN);

            // How many high fives did their posts receive?
            int high5received = 0;
            ArrayList<RelationshipType> types = new ArrayList<>();
            for (RelationshipType t : user.getRelationshipTypes()) {
                if (t.name().startsWith("POSTED_ON")) {
                    types.add(t);
                }
            }
            for (Relationship r1 : user.getRelationships(types.toArray(new RelationshipType[0]))) {
                Node post = r1.getEndNode();
                for (Relationship r : post.getRelationships(RelationshipTypes.HIGH_FIVED, Direction.INCOMING)) {
                    ZonedDateTime when = (ZonedDateTime) r.getProperty(TIME);
                    if (when.isAfter(startOfDay)) {
                        high5received++;
                    }
                }
            }

            // How many high fives did they give out today?
            int high5given = 0;
            Node user2 = Users.findUser(username2, db);
            for (Relationship r : user.getRelationships(RelationshipTypes.HIGH_FIVED, Direction.OUTGOING)) {
                if (r.getEndNodeId() == postId) {
                    throw FiveExceptions.alreadyHighFivedPost;
                }
                ZonedDateTime when = (ZonedDateTime)r.getProperty(TIME);
                if (when.isAfter(startOfDay)) {
                    high5given++;
                }
            }

            // Are they over the limit
            if (high5given - 5 >= high5received) {
                throw FiveExceptions.overHighFiveLimit;
            }
            Node post;
            try {
                post = db.getNodeById(postId);
            } catch (Exception e) {
                throw PostExceptions.postNotFound;
            }

            Relationship r2 = user.createRelationshipTo(post, RelationshipTypes.HIGH_FIVED);
            r2.setProperty(TIME, dateTime);

            results = post.getAllProperties();
            results.put(TIME, dateTime);
            results.put(USERNAME, username2);
            results.put(NAME, user2.getProperty(NAME));
            results.put(HIGH_FIVED, true);
            results.put(LOW_FIVED, false);
            results.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED, Direction.INCOMING));
            results.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED, Direction.INCOMING));


            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }
}
