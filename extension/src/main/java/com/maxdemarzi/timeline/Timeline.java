package com.maxdemarzi.timeline;

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
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.HASH;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/timeline")
public class Timeline {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    public Response getTimeline(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("100") final Integer limit,
                             @QueryParam("since") final Long since,
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
            HashSet<Long> seen = new HashSet<>();
            ArrayList<Node> follows = new ArrayList<>();

            // TODO: 4/26/18 Perform matching logic to find users and add to follows list.


            LocalDateTime earliest = LocalDateTime.ofEpochSecond((Long)user.getProperty(TIME), 0, ZoneOffset.UTC);

            while (seen.size() < limit && (dateTime.isAfter(earliest))) {
                RelationshipType posted = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Node follow : follows) {
                    Map followProperties = follow.getAllProperties();

                    for (Relationship r1 : follow.getRelationships(Direction.OUTGOING, posted)) {
                        Node post = r1.getEndNode();
                        if(seen.add(post.getId())) {
                            Long time = (Long)r1.getProperty("time");
                            Map<String, Object> properties = r1.getEndNode().getAllProperties();
                            if (time < latest) {
                                properties.put(TIME, time);
                                properties.put(USERNAME, followProperties.get(USERNAME));
                                properties.put(NAME, followProperties.get(NAME));
                                properties.put(HASH, followProperties.get(HASH));
                                results.add(properties);
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
