package com.maxdemarzi.timeline;

import com.maxdemarzi.cities.Cities;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.values.storable.CoordinateReferenceSystem;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.HASH;
import static com.maxdemarzi.schema.Properties.HIGH_FIVED;
import static com.maxdemarzi.schema.Properties.HIGH_FIVES;
import static com.maxdemarzi.schema.Properties.IS;
import static com.maxdemarzi.schema.Properties.IS_LOOKING_FOR;
import static com.maxdemarzi.schema.Properties.LOW_FIVED;
import static com.maxdemarzi.schema.Properties.LOW_FIVES;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/timeline")
public class Timeline {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CoordinateReferenceSystem crs = CoordinateReferenceSystem.Cartesian;

    @GET
    public Response getTimeline(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("100") final Integer limit,
                             @QueryParam("since") final Long since,
                             @QueryParam("city") final String city,
                             @QueryParam("state") final String state,
                             @QueryParam("distance") Integer distance,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        Long latest = dateTime.toEpochSecond(ZoneOffset.UTC);

        // 5 Miles = 8 Kilometers, 10 Miles = 16 Kilometers, 25 Miles = 40 Kilometers
        // Number multiplied by 1000 because search is in meters.
        if (distance == null) {
            distance = 40000;
        }

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            String is = (String)user.getProperty(IS);
            HashSet<String> isLookingFor = new HashSet<>(Arrays.asList((String[]) user.getProperty(IS_LOOKING_FOR)));

            HashSet<Node> highFived = new HashSet<>();
            HashSet<Node> lowFived = new HashSet<>();

            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HIGH_FIVED)) {
                highFived.add(r1.getEndNode());
            }
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LOW_FIVED)) {
                lowFived.add(r1.getEndNode());
            }

            HashSet<Long> seen = new HashSet<>();
            HashSet<Node> locations = new HashSet<>();

            // Up to 30 days ago
            LocalDateTime earliest = dateTime.minusDays(30);

            // Get the User Location(s) and Nearby Locations
            if (city == null) {
                for (Relationship inLocation : user.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_LOCATION)) {
                    Node location = inLocation.getEndNode();
                    locations.add(location);
                    locations.addAll(Cities.findCitiesNearby(location, distance, db));
                }
            } else {
                Node location = Cities.findCity(city, state, db);
                locations.add(location);
                locations.addAll(Cities.findCitiesNearby(location, distance, db));
            }

            // Get recent posts

            while (seen.size() < limit && (dateTime.isAfter(earliest))) {
                RelationshipType posted = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Node location : locations) {
                    for (Relationship inLocation : location.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                        Node person = inLocation.getStartNode();

                        for (Relationship r1 : person.getRelationships(Direction.OUTGOING, posted)) {
                            Node post = r1.getEndNode();

                            // Before adding post to timeline, check for compatibility
                            Map<String, Object> properties = person.getAllProperties();
                            String theyAre = (String) properties.get(IS);
                            HashSet<String> theyAreLookingFor = new HashSet<>(Arrays.asList((String[]) properties.get(IS_LOOKING_FOR)));

                            if (theyAreLookingFor.contains(is) && isLookingFor.contains(theyAre)) {

                                if (seen.add(post.getId())) {
                                    Long time = (Long) r1.getProperty("time");
                                    Map<String, Object> posting = r1.getEndNode().getAllProperties();
                                    if (time < latest) {
                                        posting.put(TIME, time);
                                        posting.put(USERNAME, properties.get(USERNAME));
                                        posting.put(NAME, properties.get(NAME));
                                        posting.put(HASH, properties.get(HASH));
                                        posting.put(HIGH_FIVED, highFived.contains(post));
                                        posting.put(LOW_FIVED, lowFived.contains(post));
                                        posting.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED, Direction.INCOMING));
                                        posting.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED, Direction.INCOMING));
                                        results.add(posting);
                                    }
                                }
                            }
                        }
                    }
                }
                dateTime = dateTime.minusDays(1);
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get("time"), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

}
