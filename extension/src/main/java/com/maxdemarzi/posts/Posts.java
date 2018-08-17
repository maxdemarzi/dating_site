package com.maxdemarzi.posts;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.mentions.Mentions;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.tags.Tags;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.*;
import static com.maxdemarzi.users.Users.getPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/posts")
public class Posts {

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());
    private static final Comparator<RelationshipType> relTypeComparator = Comparator.comparing((Function<RelationshipType, Object>) RelationshipType::name, reverseOrder());

    @GET
    public Response getPosts(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final String since,
                             @QueryParam("username2") final String username2,
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
            Node user2;
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
            int count = 0;
            ArrayList<RelationshipType> types = new ArrayList<>();
            user.getRelationshipTypes().forEach(t-> {
                        if (t.name().startsWith("POSTED_ON")) {
                            types.add(t);
                        }
                    });
            types.sort(relTypeComparator);

            for (RelationshipType relType : types) {
                if (count >= limit) { break;}
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, relType)) {
                    Node post = r1.getEndNode();
                    Map<String, Object> result = post.getAllProperties();
                    ZonedDateTime time = (ZonedDateTime)r1.getProperty("time");
                    if(time.isBefore(latest)) {
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
            }
            tx.success();
        }

        results.sort(timedComparator);

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @POST
    public Response createPost(String body, @PathParam("username") final String username,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);
        ZonedDateTime dateTime = ZonedDateTime.now(utc);

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

    private Node createPost(@Context GraphDatabaseService db, HashMap input, Node user, ZonedDateTime dateTime) {
        Node post = db.createNode(Labels.Post);
        post.setProperty(STATUS, input.get("status"));
        post.setProperty(TIME, dateTime);
        Relationship r1 = user.createRelationshipTo(post, RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter)));
        r1.setProperty(TIME, dateTime);
        Tags.createTags(post, input, dateTime, db);
        Mentions.createMentions(post, input, dateTime, db);
        return post;
    }


    @PUT
    @Path("/{time}")
    public Response updatePost(String body,
                               @PathParam("username") final String username,
                               @PathParam("time") final String time,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = getPost(user, ZonedDateTime.parse(time));
            post.setProperty(STATUS, input.get(STATUS));
            ZonedDateTime dateTime = (ZonedDateTime)post.getProperty(TIME);
            Tags.createTags(post, input, dateTime, db);
            Mentions.createMentions(post, input, dateTime, db);
            results = post.getAllProperties();
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(HIGH_FIVED, false);
            results.put(LOW_FIVED, false);
            results.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED, Direction.INCOMING));
            results.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED, Direction.INCOMING));

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    @Path("/{username2}/{time}/reply")
    public Response createReply(String body, @PathParam("username") final String username,
                                @PathParam("username2") final String username2,
                                @PathParam("time") final String time,
                                @Context GraphDatabaseService db) throws IOException {

        Map<String, Object> results;
        HashMap input = PostValidator.validate(body);
        ZonedDateTime dateTime = ZonedDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = createPost(db, input, user, dateTime);
            results = post.getAllProperties();
            results.put(TIME, dateTime);
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(HIGH_FIVED, false);
            results.put(LOW_FIVED, false);
            results.put(HIGH_FIVES, 0);
            results.put(LOW_FIVES, 0);

            Node user2 = Users.findUser(username2, db);
            Node post2 = getPost(user2, ZonedDateTime.parse(time));
            Relationship r2 = post.createRelationshipTo(post2, RelationshipTypes.REPLIED_TO);
            r2.setProperty(TIME, dateTime);

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static Node getAuthor(Node post, ZonedDateTime time) {
        RelationshipType original = RelationshipType.withName("POSTED_ON_" +
                time.format(dateFormatter));
        return post.getSingleRelationship(original, Direction.INCOMING).getStartNode();
    }

}
