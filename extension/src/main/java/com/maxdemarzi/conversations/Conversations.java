package com.maxdemarzi.conversations;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.posts.PostValidator;
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
import java.time.*;
import java.util.*;

import static com.maxdemarzi.Time.getLatestTime;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.*;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/conversations")
public class Conversations {
    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    @Path("/{username2}")
    public Response getConversation(@PathParam("username") final String username,
                                    @PathParam("username2") final String username2,
                                     @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                     @QueryParam("since") final String since,
                                     @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        ZonedDateTime latest = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = Users.findUser(username2, db);

            // Find the conversation between these two users
            Node conversation = null;
            outerloop:
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.PART_OF)) {
                conversation = r1.getEndNode();
                for (Relationship r2 : conversation.getRelationships(Direction.INCOMING, RelationshipTypes.PART_OF)) {
                    if (user2.equals(r2.getStartNode())) {
                        break outerloop;
                    }
                }
            }
            if (conversation == null) {
                throw ConversationExceptions.conversationNotFound;
            }

            for (Relationship r1 :  conversation.getRelationships(Direction.INCOMING, RelationshipTypes.ADDED_TO)) {
                Node message = r1.getStartNode();
                if (latest.isAfter((ZonedDateTime) message.getProperty(TIME))) {
                    results.add(message.getAllProperties());
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
    public Response addToConversation(String body,
                                      @PathParam("username") final String username,
                                      @PathParam("username2") final String username2,
                                      @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);
        ZonedDateTime dateTime = ZonedDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = Users.findUser(username2, db);

            // Find the conversation between these two users
            Node conversation = null;
            outerloop:
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.PART_OF)) {
                conversation = r1.getEndNode();
                for (Relationship r2 : conversation.getRelationships(Direction.INCOMING, RelationshipTypes.PART_OF)) {
                    if (user2.equals(r2.getStartNode())) {
                        break outerloop;
                    }
                }
            }
            // Are we allowed to have a conversation with this user?
            if (conversation == null) {
                // Are they blocking us?
                HashSet<Node> blocked = new HashSet<>();
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                    blocked.add(r1.getEndNode());
                }
                if (blocked.contains(user)) {
                    throw ConversationExceptions.conversationNotAllowed;
                }

                // Do we have a recent high five from them?
                // Get their time zone first
                ZoneId zoneId = ZoneId.of((String) user.getProperty(TIMEZONE));
                ZonedDateTime startOfFiveDays = ZonedDateTime.now(zoneId).with(LocalTime.MIN).minusDays(5);

                // Get their posts
                ArrayList<RelationshipType> types = new ArrayList<>();
                for (RelationshipType t : user.getRelationshipTypes()) {
                    if (t.name().startsWith("POSTED_ON")) {
                        types.add(t);
                    }
                }
                // Check their posts for a high five from user2
                boolean allowed = false;
                outerloop:
                for (Relationship r1 : user.getRelationships(types.toArray(new RelationshipType[0]))) {
                    Node post = r1.getEndNode();
                    for (Relationship r : post.getRelationships(RelationshipTypes.HIGH_FIVED, Direction.INCOMING)) {
                        // Check the user first, then get the time
                        if (user2.equals(r.getStartNode())) {
                            ZonedDateTime when = (ZonedDateTime)r.getProperty(TIME);
                            if (when.isAfter(startOfFiveDays)) {
                                allowed = true;
                                break outerloop;
                            }
                        }
                    }
                }
                if (allowed) {
                    conversation = db.createNode(Labels.Conversation);
                    user.createRelationshipTo(conversation, RelationshipTypes.PART_OF);
                    user2.createRelationshipTo(conversation, RelationshipTypes.PART_OF);
                } else {
                    throw ConversationExceptions.conversationNotAllowed;
                }
            }

            Node message = db.createNode(Labels.Message);
            message.setProperty(STATUS, input.get(STATUS));
            message.setProperty(TIME, dateTime);
            message.setProperty(AUTHOR, username);
            message.createRelationshipTo(conversation, RelationshipTypes.ADDED_TO);

            results = message.getAllProperties();
            tx.success();
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    public Response getConversations(@PathParam("username") final String username,
                                     @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                     @QueryParam("since") final String since,
                                     @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        ZonedDateTime latest = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);

            HashSet<Node> blocked = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                blocked.add(r1.getEndNode());
            }

            int count = 0;
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.PART_OF)) {
                if (count >= limit) {
                    break;
                }

                Node conversation = r1.getEndNode();
                Node other = null;
                for (Relationship r2 : conversation.getRelationships(Direction.INCOMING, RelationshipTypes.PART_OF)) {
                    if (user.equals(r2.getStartNode())) {
                        continue;
                    }
                    other = r2.getStartNode();
                }
                if (blocked.contains(other) || other == null) {
                    continue;
                }

                // We need to get the user and username of the last message, as well as the date and status of the last message
                // along with who wrote it.
                Node message = null;
                ZonedDateTime last = ZonedDateTime.of(1979,3,4,8,30,0,0, ZoneId.systemDefault());
                for (Relationship r2 : conversation.getRelationships(Direction.INCOMING, RelationshipTypes.ADDED_TO)) {
                    if (message == null) {
                        message = r2.getStartNode();
                        last = (ZonedDateTime) message.getProperty(TIME);
                        continue;
                    }
                    if (last.isBefore((ZonedDateTime) r2.getStartNode().getProperty(TIME))) {
                        message = r2.getStartNode();
                        last = (ZonedDateTime) message.getProperty(TIME);
                    }
                }
                if (last.isBefore(latest)) {
                    if (message != null) {
                        Map<String, Object> result = new HashMap<>();
                        result.put(USERNAME, other.getProperty(USERNAME));
                        result.put(NAME, other.getProperty(NAME));
                        result.put(HASH, other.getProperty(HASH));
                        result.put(TIME, last);
                        result.put(STATUS, message.getProperty(STATUS));
                        result.put(AUTHOR, message.getProperty(AUTHOR));
                        results.add(result);
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

}