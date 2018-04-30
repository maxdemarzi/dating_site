package com.maxdemarzi.posts;

import com.maxdemarzi.mentions.Mentions;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.tags.Tags;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.HASH;
import static com.maxdemarzi.schema.Properties.HIGH_FIVED;
import static com.maxdemarzi.schema.Properties.HIGH_FIVES;
import static com.maxdemarzi.schema.Properties.LOW_FIVED;
import static com.maxdemarzi.schema.Properties.LOW_FIVES;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.STATUS;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static com.maxdemarzi.users.Users.getPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/posts")
public class Posts {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    public Response getPosts(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final Long since,
                             @QueryParam("username2") final String username2,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        Long latest = dateTime.toEpochSecond(ZoneOffset.UTC);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = null;
            HashSet<Node> highFived = new HashSet<>();
            HashSet<Node> lowFived = new HashSet<>();
            if (username2 != null) {
                user2 = Users.findUser(username2, db);
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.HIGH_FIVED)) {
                    highFived.add(r1.getEndNode());
                }
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.LOW_FIVED)) {
                    lowFived.add(r1.getEndNode());
                }
            }


            Map userProperties = user.getAllProperties();
            LocalDateTime earliest = LocalDateTime.ofEpochSecond((Long)userProperties.get(TIME), 0, ZoneOffset.UTC);
            int count = 0;
            while (count < limit && (dateTime.isAfter(earliest))) {
                RelationshipType relType = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, relType)) {
                    Node post = r1.getEndNode();
                    Map<String, Object> result = post.getAllProperties();
                    Long time = (Long)r1.getProperty("time");
                    if(time < latest) {
                        result.put(TIME, time);
                        result.put(USERNAME, username);
                        result.put(NAME, userProperties.get(NAME));
                        result.put(HASH, userProperties.get(HASH));
                        result.put(HIGH_FIVED, highFived.contains(post));
                        result.put(LOW_FIVED, lowFived.contains(post));
                        result.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED, Direction.INCOMING));
                        result.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED, Direction.INCOMING));

                        results.add(result);
                        count++;
                    }
                }
                dateTime = dateTime.minusDays(1);
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    public Response createPost(String body, @PathParam("username") final String username,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);
        LocalDateTime dateTime = LocalDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = createPost(db, input, user, dateTime);
            results = post.getAllProperties();
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(HASH, user.getProperty(HASH));
            results.put(HIGH_FIVED, false);
            results.put(LOW_FIVED, false);
            results.put(HIGH_FIVES, 0);
            results.put(LOW_FIVES, 0);

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    private Node createPost(@Context GraphDatabaseService db, HashMap input, Node user, LocalDateTime dateTime) {
        Node post = db.createNode(Labels.Post);
        post.setProperty(STATUS, input.get("status"));
        post.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
        Relationship r1 = user.createRelationshipTo(post, RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter)));
        r1.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
        Tags.createTags(post, input, dateTime, db);
        Mentions.createMentions(post, input, dateTime, db);
        return post;
    }


    @PUT
    @Path("/{time}")
    public Response updatePost(String body,
                               @PathParam("username") final String username,
                               @PathParam("time") final Long time,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = getPost(user, time);
            post.setProperty(STATUS, input.get(STATUS));
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond((Long)post.getProperty(TIME), 0, ZoneOffset.UTC);
            Tags.createTags(post, input, dateTime, db);
            Mentions.createMentions(post, input, dateTime, db);
            results = post.getAllProperties();
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(HIGH_FIVED, false);
            results.put(LOW_FIVED, false);
            results.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED ,Direction.INCOMING));
            results.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED ,Direction.INCOMING));

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    @Path("/{username2}/{time}/reply")
    public Response createReply(String body, @PathParam("username") final String username,
                                @PathParam("username2") final String username2,
                                @PathParam("time") final Long time,
                                @Context GraphDatabaseService db) throws IOException {

        Map<String, Object> results;
        HashMap input = PostValidator.validate(body);
        LocalDateTime dateTime = LocalDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = createPost(db, input, user, dateTime);
            results = post.getAllProperties();
            results.put(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(HIGH_FIVED, false);
            results.put(LOW_FIVED, false);
            results.put(HIGH_FIVES, 0);
            results.put(LOW_FIVES, 0);

            Node user2 = Users.findUser(username2, db);
            Node post2 = getPost(user2, time);
            Relationship r2 = post.createRelationshipTo(post2, RelationshipTypes.REPLIED_TO);
            r2.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static Node getAuthor(Node post, Long time) {
        LocalDateTime postedDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
        RelationshipType original = RelationshipType.withName("POSTED_ON_" +
                postedDateTime.format(dateFormatter));
        return post.getSingleRelationship(original, Direction.INCOMING).getStartNode();
    }

}
