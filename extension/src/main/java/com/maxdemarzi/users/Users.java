package com.maxdemarzi.users;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.*;

@Path("/users")
public class Users {

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();

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
        String username = ((String)parameters.get(USERNAME)).toLowerCase();
        String email = ((String)parameters.get(EMAIL)).toLowerCase();
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                user = db.findNode(Labels.User, EMAIL, email);
                if (user == null) {
                    user = db.createNode(Labels.User);
                    user.setProperty(EMAIL, email);
                    user.setProperty(NAME, parameters.get(NAME));
                    user.setProperty(BIO, parameters.get(BIO));
                    user.setProperty(USERNAME, username);
                    user.setProperty(PASSWORD, parameters.get(PASSWORD));
                    user.setProperty(IS, parameters.get(IS));
                    user.setProperty(IS_LOOKING_FOR, parameters.get(IS_LOOKING_FOR));
                    user.setProperty(HASH, new Md5Hash(email).toString());
                    user.setProperty(TIME, ZonedDateTime.now(utc));
                    user.setProperty(DISTANCE, parameters.get(DISTANCE));

                    Node city = db.findNode(Labels.City, FULL_NAME, parameters.get(CITY));
                    user.createRelationshipTo(city, RelationshipTypes.IN_LOCATION);
                    Node state = city.getSingleRelationship(RelationshipTypes.IN_LOCATION, Direction.OUTGOING).getEndNode();
                    Node timezone = state.getSingleRelationship(RelationshipTypes.IN_TIMEZONE, Direction.OUTGOING).getEndNode();
                    user.setProperty(TIMEZONE, timezone.getProperty(NAME));

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

                    Node state = city.getSingleRelationship(RelationshipTypes.IN_LOCATION, Direction.OUTGOING).getEndNode();
                    Node timezone = state.getSingleRelationship(RelationshipTypes.IN_TIMEZONE, Direction.OUTGOING).getEndNode();
                    user.setProperty(TIMEZONE, timezone.getProperty(NAME));
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
        Node user = db.findNode(Labels.User, USERNAME, username.toLowerCase());
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
        Integer locations = user.getDegree(RelationshipTypes.IN_LOCATION, Direction.OUTGOING);
        Integer posts = user.getDegree(Direction.OUTGOING) - likes - hates - highFives - lowFives - has - wants - locations;
        results.put(LIKES, likes);
        results.put(HATES, hates);
        results.put(LOW_FIVES, lowFives);
        results.put(HIGH_FIVES, highFives);
        results.put(HAS, has);
        results.put(WANTS, wants);
        results.put(POSTS, posts);
        String city = (String)user.getSingleRelationship(RelationshipTypes.IN_LOCATION, Direction.OUTGOING).getEndNode().getProperty(FULL_NAME);
        results.put(CITY, city);
        return results;
    }
}