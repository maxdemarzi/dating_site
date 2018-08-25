package com.maxdemarzi.blocks;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.schema.RelationshipTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.maxdemarzi.Time.getLatestTime;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static com.maxdemarzi.users.Users.findUser;
import static com.maxdemarzi.users.Users.getUserAttributes;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/blocks")
public class Blocks {

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    public Response getBlocks(@PathParam("username") final String username,
                              @QueryParam("limit") @DefaultValue("25") final Integer limit,
                              @QueryParam("since") final String since,
                              @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        ZonedDateTime latest = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                Node blocked = r1.getEndNode();
                ZonedDateTime time = (ZonedDateTime)r1.getProperty("time");
                if(time.isBefore(latest)) {
                    Map<String, Object> result = getUserAttributes(blocked);
                    result.put(TIME, time);
                    results.add(result);
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
    @Path("/{username2}")
    public Response createBlocks(@PathParam("username") final String username,
                                  @PathParam("username2") final String username2,
                                  @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results =  new HashMap<>();
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            Node user2 = findUser(username2, db);

            if (user.getDegree(RelationshipTypes.BLOCKS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.BLOCKS, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS) ) {
                    if (r1.getEndNode().equals(user2)) {
                        throw BlockExceptions.alreadyBlockingUser;
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.BLOCKS)) {
                    if (r1.getStartNode().equals(user)) {
                        throw BlockExceptions.alreadyBlockingUser;
                    }
                }
           }
            
            Relationship blocks = user.createRelationshipTo(user2, RelationshipTypes.BLOCKS);
            blocks.setProperty(TIME, ZonedDateTime.now(utc));

            results.put(USERNAME, username2);
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @DELETE
    @Path("/{username2}")
    public Response removeBlocks(@PathParam("username") final String username,
                                 @PathParam("username2") final String username2,
                                 @Context GraphDatabaseService db) throws IOException {
        boolean deleted = false;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            Node user2 = findUser(username2, db);

            if (user.getDegree(RelationshipTypes.BLOCKS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.BLOCKS, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS) ) {
                    if (r1.getEndNode().equals(user2)) {
                        r1.delete();
                        deleted = true;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.BLOCKS)) {
                    if (r1.getStartNode().equals(user)) {
                        r1.delete();
                        deleted = true;
                        break;
                    }
                }
            }

            tx.success();
        }

        if (deleted) {
            return Response.noContent().build();
        } else {
            throw BlockExceptions.notBlockingUser;
        }
    }
}
