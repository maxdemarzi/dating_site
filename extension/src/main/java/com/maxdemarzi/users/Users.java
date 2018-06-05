package com.maxdemarzi.users;

import com.maxdemarzi.posts.PostExceptions;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.BIO;
import static com.maxdemarzi.schema.Properties.CITY;
import static com.maxdemarzi.schema.Properties.DISTANCE;
import static com.maxdemarzi.schema.Properties.EMAIL;
import static com.maxdemarzi.schema.Properties.FULL_NAME;
import static com.maxdemarzi.schema.Properties.HAS;
import static com.maxdemarzi.schema.Properties.HASH;
import static com.maxdemarzi.schema.Properties.HATES;
import static com.maxdemarzi.schema.Properties.HAVE;
import static com.maxdemarzi.schema.Properties.HIGH_FIVES;
import static com.maxdemarzi.schema.Properties.IS;
import static com.maxdemarzi.schema.Properties.IS_LOOKING_FOR;
import static com.maxdemarzi.schema.Properties.LIKES;
import static com.maxdemarzi.schema.Properties.LOW_FIVES;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.PASSWORD;
import static com.maxdemarzi.schema.Properties.POSTS;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static com.maxdemarzi.schema.Properties.WANT;
import static com.maxdemarzi.schema.Properties.WANTS;

@Path("/users")
public class Users {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/{username}")
    public Response getUser(@PathParam("username") final String username, @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            results = user.getAllProperties();
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/{username}/profile")
    public Response getProfile(@PathParam("username") final String username,
                               @QueryParam("username2") final String username2,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            results = getUserAttributes(user);
            HashSet<Node> userHas = new HashSet<>();
            HashSet<Node> userWants = new HashSet<>();

            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                userHas.add(r1.getEndNode());
            }
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                userWants.add(r1.getEndNode());
            }

            if (username2 != null && !username.equals(username2)) {
                Node user2 = findUser(username2, db);

                HashSet<Node> user2Has = new HashSet<>();
                HashSet<Node> user2Wants = new HashSet<>();
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                    user2Has.add(r1.getEndNode());
                }
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                    user2Wants.add(r1.getEndNode());
                }

                results.put(HAVE, userHas.stream().filter(user2Wants::contains).count());
                results.put(WANT, userWants.stream().filter(user2Has::contains).count());

            }

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    public Response createUser(String body, @Context GraphDatabaseService db) throws IOException {
        HashMap parameters = UserValidator.validate(body);
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, parameters.get(USERNAME));
            if (user == null) {
                user = db.findNode(Labels.User, EMAIL, parameters.get(EMAIL));
                if (user == null) {
                    user = db.createNode(Labels.User);
                    user.setProperty(EMAIL, parameters.get(EMAIL));
                    user.setProperty(NAME, parameters.get(NAME));
                    user.setProperty(BIO, parameters.get(BIO));
                    user.setProperty(USERNAME, parameters.get(USERNAME));
                    user.setProperty(PASSWORD, parameters.get(PASSWORD));
                    user.setProperty(IS, parameters.get(IS));
                    user.setProperty(IS_LOOKING_FOR, parameters.get(IS_LOOKING_FOR));
                    user.setProperty(HASH, new Md5Hash(((String)parameters.get(EMAIL)).toLowerCase()).toString());

                    LocalDateTime dateTime = LocalDateTime.now(utc);
                    user.setProperty(TIME, dateTime.truncatedTo(ChronoUnit.DAYS).toEpochSecond(ZoneOffset.UTC));

                    user.setProperty(DISTANCE, parameters.get(DISTANCE));

                    Node city = db.findNode(Labels.City, FULL_NAME, parameters.get(CITY));
                    user.createRelationshipTo(city, RelationshipTypes.IN_LOCATION);

                    results = user.getAllProperties();
                } else {
                    throw UserExceptions.existingEmailParameter;
                }
            } else {
                throw UserExceptions.existingUsernameParameter;
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @PUT
    public Response updateUser(String body, @Context GraphDatabaseService db) throws IOException {
        HashMap parameters = UserValidator.update(body);
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, parameters.get(USERNAME));
            if (user != null) {
                if(parameters.containsKey(EMAIL)) { user.setProperty(EMAIL, parameters.get(EMAIL)); }
                if(parameters.containsKey(NAME)) { user.setProperty(NAME, parameters.get(NAME)); }
                if(parameters.containsKey(PASSWORD)) { user.setProperty(PASSWORD, parameters.get(PASSWORD)); }
                if(parameters.containsKey(IS)) { user.setProperty(IS, parameters.get(IS)); }
                if(parameters.containsKey(IS_LOOKING_FOR)) { user.setProperty(IS_LOOKING_FOR, parameters.get(IS_LOOKING_FOR)); }
                if(parameters.containsKey(HASH)) { user.setProperty(HASH, new Md5Hash(((String)parameters.get(EMAIL)).toLowerCase()).toString()); }

                if(parameters.containsKey(DISTANCE)) { user.setProperty(DISTANCE, parameters.get(DISTANCE)); }
                if(parameters.containsKey(CITY)) {
                    Node city = db.findNode(Labels.City, FULL_NAME, parameters.get(CITY));
                    user.getSingleRelationship(RelationshipTypes.IN_LOCATION, Direction.OUTGOING).delete();
                    user.createRelationshipTo(city, RelationshipTypes.IN_LOCATION);
                }

                results = user.getAllProperties();

            } else {
                throw UserExceptions.userNotFound;
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static Node findUser(String username, @Context GraphDatabaseService db) {
        if (username == null) { return null; }
        Node user = db.findNode(Labels.User, USERNAME, username);
        if (user == null) { throw UserExceptions.userNotFound; }
        return user;
    }

    public static Map<String, Object> getUserAttributes(Node user) {
        Map<String, Object> results;
        results = user.getAllProperties();
        results.remove(EMAIL);
        results.remove(PASSWORD);
        Integer likes = user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING);
        Integer hates = user.getDegree(RelationshipTypes.HATES, Direction.OUTGOING);
        Integer highFives = user.getDegree(RelationshipTypes.HIGH_FIVED, Direction.OUTGOING);
        Integer lowFives = user.getDegree(RelationshipTypes.LOW_FIVED, Direction.OUTGOING);
        Integer has = user.getDegree(RelationshipTypes.HAS, Direction.OUTGOING);
        Integer wants = user.getDegree(RelationshipTypes.WANTS, Direction.OUTGOING);
        Integer posts = user.getDegree(Direction.OUTGOING) - likes - hates - highFives - lowFives - has - wants;
        results.put(LIKES, likes);
        results.put(HATES, hates);
        results.put(LOW_FIVES, lowFives);
        results.put(HIGH_FIVES, highFives);
        results.put(HAS, has);
        results.put(WANTS, wants);
        results.put(POSTS, posts);
        return results;
    }

    public static Node getPost(Node author, Long time) {
        LocalDateTime postedDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
        RelationshipType original = RelationshipType.withName("POSTED_ON_" +
                postedDateTime.format(dateFormatter));
        Node post = null;
        for(Relationship r1 : author.getRelationships(Direction.OUTGOING, original)) {
            Node potential = r1.getEndNode();
            if (time.equals(potential.getProperty(TIME))) {
                post = potential;
                break;
            }
        }
        if(post == null) { throw PostExceptions.postNotFound; };

        return post;
    }


}