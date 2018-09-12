package com.maxdemarzi.timeline;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.cities.Cities;
import com.maxdemarzi.schema.RelationshipTypes;
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

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.*;
import static java.util.Collections.reverseOrder;

@Path("/latest")
public class Latest {
    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    public Response getTimeline(@QueryParam("username") final String username,
                                @QueryParam("geoid") final String geoid,
                                @QueryParam("limit") @DefaultValue("100") final Integer limit,
                                @QueryParam("distance") @DefaultValue("40000") Long distance,
                                @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        HashSet<Long> seen = new HashSet<>();

        // Up to 90 days ago
        ZonedDateTime dateTime = ZonedDateTime.now(utc);
        ZonedDateTime earliest = ZonedDateTime.now(utc).minusDays(90);

        try (Transaction tx = db.beginTx()) {
            HashSet<Node> blocked = new HashSet<>();
            HashSet<Node> highFived = new HashSet<>();
            HashSet<Node> lowFived = new HashSet<>();
            Node user = null;

            if (username != null) {
                user = Users.findUser(username, db);

                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HIGH_FIVED)) {
                    highFived.add(r1.getEndNode());
                }
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LOW_FIVED)) {
                    lowFived.add(r1.getEndNode());
                }

                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                    blocked.add(r1.getEndNode());
                }
            }

            HashSet<Node> locations = new HashSet<>();
            if (user != null && geoid == null) {
                for (Relationship inLocation : user.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_LOCATION)) {
                    Node location = inLocation.getEndNode();
                    locations.add(location);
                    locations.addAll(Cities.findCitiesNearby(location, distance, db));
                }
            }
            if (geoid != null) {
                Node city = Cities.findCity(geoid, db);
                locations.add(city);
                locations.addAll(Cities.findCitiesNearby(city, distance, db));
            }

            // Get recent posts
            while (seen.size() < limit && (dateTime.isAfter(earliest))) {
                RelationshipType posted = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Node location : locations) {
                    for (Relationship inLocation : location.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                        Node person = inLocation.getStartNode();
                        if(!blocked.contains(person)) {
                            Map<String, Object> properties = person.getAllProperties();
                            for (Relationship r1 : person.getRelationships(Direction.OUTGOING, posted)) {
                                Node post = r1.getEndNode();
                                if (seen.add(post.getId())) {
                                    ZonedDateTime time = (ZonedDateTime) r1.getProperty("time");
                                    Map<String, Object> posting = r1.getEndNode().getAllProperties();
                                    posting.put(ID, post.getId());
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
                dateTime = dateTime.minusDays(1);
            }
            tx.success();
        }

        results.sort(timedComparator);

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

}