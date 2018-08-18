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

@Path("/users/{username}/timeline")
public class Timeline {

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    @GET
    public Response getTimeline(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("100") final Integer limit,
                             @QueryParam("since") final String since,
                             @QueryParam("city") final String city,
                             @QueryParam("state") final String state,
                             @QueryParam("distance") @DefaultValue("40000") Integer distance,
                             @QueryParam("competition") @DefaultValue("false") Boolean competition,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        ZonedDateTime dateTime;
        ZonedDateTime latest;
        if (since == null) {
            latest = ZonedDateTime.now(utc);
            dateTime = ZonedDateTime.now(utc);
        } else {
            latest = ZonedDateTime.parse(since);
            dateTime = ZonedDateTime.parse(since);
        }

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Map userProperties = user.getAllProperties();
            String is = (String)userProperties.get(IS);
            HashSet<String> isLookingFor = new HashSet<>(Arrays.asList((String[]) userProperties.get(IS_LOOKING_FOR)));

            HashSet<Node> highFived = new HashSet<>();
            HashSet<Node> lowFived = new HashSet<>();

            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HIGH_FIVED)) {
                highFived.add(r1.getEndNode());
            }
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LOW_FIVED)) {
                lowFived.add(r1.getEndNode());
            }

            HashSet<Node> blocked = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                blocked.add(r1.getEndNode());
            }

            HashSet<Long> seen = new HashSet<>();

            // Up to the day the user registered
            ZonedDateTime earliest = ((ZonedDateTime)userProperties.get(TIME)).minusDays(90);

            // Get the User Location(s) and Nearby Locations
            HashSet<Node> locations = new HashSet<>();
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

                            // Before adding post to timeline, check for compatibility or competition
                            Map<String, Object> properties = person.getAllProperties();
                            String theyAre = (String) properties.get(IS);
                            HashSet<String> theyAreLookingFor = new HashSet<>(Arrays.asList((String[]) properties.get(IS_LOOKING_FOR)));

                            boolean include;
                            if (competition) {
                                include = (theyAreLookingFor.stream().anyMatch(isLookingFor::contains)) &&
                                        theyAre.equals(is);
                            } else {
                                include = theyAreLookingFor.contains(is) && isLookingFor.contains(theyAre);
                            }

                            if (include && !blocked.contains(person)) {
                                if (seen.add(post.getId())) {
                                    ZonedDateTime time = (ZonedDateTime)r1.getProperty("time");
                                    Map<String, Object> posting = r1.getEndNode().getAllProperties();
                                    if(time.isBefore(latest)) {
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
