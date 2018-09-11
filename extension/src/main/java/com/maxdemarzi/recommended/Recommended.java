package com.maxdemarzi.recommended;

import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.cities.Cities;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Pair;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static com.maxdemarzi.schema.Properties.*;
import static com.maxdemarzi.users.Users.getUserAttributes;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/recommended")
public class Recommended {
    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Pair<Long, Double>> pointsComparator = Comparator.comparing(Pair::other, reverseOrder());

    @GET
    public Response getRecommended(@PathParam("username") final String username,
                                @QueryParam("limit") @DefaultValue("100") final Integer limit,
                                @QueryParam("city") final String city,
                                @QueryParam("state") final String state,
                                @QueryParam("distance") @DefaultValue("40000") Long distance,
                                @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Map userProperties = user.getAllProperties();
            String is = (String)userProperties.get(IS);
            HashSet<String> isLookingFor = new HashSet<>(Arrays.asList((String[]) userProperties.get(IS_LOOKING_FOR)));

            HashSet<Node> blocked = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                blocked.add(r1.getEndNode());
            }

            HashSet<Long> have = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                have.add(r1.getEndNodeId());
            }

            HashSet<Long> want = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                want.add(r1.getEndNodeId());
            }

            HashSet<Long> like = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                like.add(r1.getEndNodeId());
            }

            HashSet<Long> hate = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HATES)) {
                hate.add(r1.getEndNodeId());
            }

            // Get the User Location(s) and Nearby Locations
            HashSet<Node> locations = new HashSet<>();
            if (city == null) {
                for (Relationship inLocation : user.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_LOCATION)) {
                    Node location = inLocation.getEndNode();
                    locations.add(location);
                    locations.addAll(Cities.findCitiesNearby(location, (Long)userProperties.getOrDefault(DISTANCE, distance), db));
                }
            } else {
                Node location = Cities.findCity(city, state, db);
                locations.add(location);
                locations.addAll(Cities.findCitiesNearby(location, distance, db));
            }

            // Get recommended users by points
            ArrayList<Pair<Long, Double>> people = new ArrayList<>();

            for (Node location : locations) {
                for (Relationship inLocation : location.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                    Node person = inLocation.getStartNode();
                    if (user.equals(person)) { continue; }
                    Map<String, Object> properties = person.getAllProperties();
                    String theyAre = (String) properties.get(IS);
                    HashSet<String> theyAreLookingFor = new HashSet<>(Arrays.asList((String[]) properties.get(IS_LOOKING_FOR)));

                    boolean include =  theyAreLookingFor.contains(is) && isLookingFor.contains(theyAre) && !blocked.contains(person);

                    if (include) {
                        double points = 0.0;
                        Set<Long> theyHave = new HashSet<>();
                        Set<Long> theyWant = new HashSet<>();
                        Set<Long> theyLike = new HashSet<>();
                        Set<Long> theyHate = new HashSet<>();
                        for (Relationship r1 : person.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                            theyHave.add(r1.getEndNodeId());
                        }
                        for (Relationship r1 : person.getRelationships(Direction.OUTGOING, RelationshipTypes.WANTS)) {
                            theyWant.add(r1.getEndNodeId());
                        }
                        for (Relationship r1 : person.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                            theyLike.add(r1.getEndNodeId());
                        }
                        for (Relationship r1 : person.getRelationships(Direction.OUTGOING, RelationshipTypes.HATES)) {
                            theyHate.add(r1.getEndNodeId());
                        }

                        points += 2.0 * jaccardSimilarity(want, theyHave);
                        points += 2.0 * jaccardSimilarity(have, theyWant);
                        points += jaccardSimilarity(like, theyLike);
                        points += jaccardSimilarity(hate, theyHate);
                        // lose points for the opposite
                        points -= jaccardSimilarity(like, theyHate);
                        points -= jaccardSimilarity(hate, theyLike);

                        if (points > 0.0) {
                            people.add(Pair.of(person.getId(), points));
                        }
                    }
                }
            }

            people.sort(pointsComparator);
            for (Pair<Long, Double> pair : people.subList(0, Math.min(people.size(), limit))) {
                Node person = db.getNodeById(pair.first());
                Map<String, Object> result = getUserAttributes(person);
                result.put(POINTS, pair.other());
                results.add(result);
            }

            tx.success();
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results))
                .build();
    }

    static private double jaccardSimilarity(Set<Long> s1, Set<Long> s2) {
        Set<Long> clone = new HashSet<>(s1);
        final int sa = s1.size();
        final int sb = s2.size();
        clone.retainAll(s2);
        final int intersection = clone.size();
        if (sa + sb - intersection == 0) { return 0.0D; }
        return 1.0D * intersection / ( sa + sb - intersection);
    }
}
