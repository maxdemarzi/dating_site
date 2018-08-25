package com.maxdemarzi.mentions;

import com.maxdemarzi.CustomObjectMapper;
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
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.getLatestTime;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.schema.Properties.*;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/mentions")
public class Mentions {

    private static final Pattern mentionsPattern = Pattern.compile("@(\\S+)");

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());


    @GET
    public Response getMentions(@PathParam("username") final String username,
                                @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                @QueryParam("since") final String since,
                                @QueryParam("username2") final String username2,
                                @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        ZonedDateTime dateTime = getLatestTime(since);
        ZonedDateTime latest = getLatestTime(since);

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

            HashSet<Node> blocked = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                blocked.add(r1.getEndNode());
            }
            ZonedDateTime earliest = (ZonedDateTime)user.getProperty(TIME);
            int count = 0;
            while (count < limit && (dateTime.isAfter(earliest))) {
                RelationshipType relType = RelationshipType.withName("MENTIONED_ON_" +
                        dateTime.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.INCOMING, relType)) {
                    Node post = r1.getStartNode();
                    Map<String, Object> result = post.getAllProperties();
                    ZonedDateTime time = (ZonedDateTime)r1.getProperty("time");
                    if(time.isBefore(latest)) {
                        Node author = getAuthor(post, time);
                        if (!blocked.contains(author)) {
                            result.put(TIME, time);
                            result.put(USERNAME, author.getProperty(USERNAME));
                            result.put(NAME, author.getProperty(NAME));
                            result.put(HASH, author.getProperty(HASH));
                            result.put(HIGH_FIVED, highFived.contains(post));
                            result.put(LOW_FIVED, lowFived.contains(post));
                            result.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED ,Direction.INCOMING));
                            result.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED ,Direction.INCOMING));

                            results.add(result);
                            count++;
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

    public static void createMentions(Node post, HashMap<String, Object> input, ZonedDateTime dateTime, GraphDatabaseService db) {
        Matcher mat = mentionsPattern.matcher(((String)input.get("status")).toLowerCase());

        for (Relationship r1 : post.getRelationships(Direction.OUTGOING, RelationshipType.withName("MENTIONED_ON_" +
                dateTime.format(dateFormatter)))) {
            r1.delete();
        }

        Set<Node> mentioned = new HashSet<>();
        while (mat.find()) {
            String username = mat.group(1);
            Node user = db.findNode(Labels.User, USERNAME, username);
            if (user != null && !mentioned.contains(user)) {
                Relationship r1 = post.createRelationshipTo(user, RelationshipType.withName("MENTIONED_ON_" +
                        dateTime.format(dateFormatter)));
                r1.setProperty(TIME, dateTime);
                mentioned.add(user);
            }
        }
    }
}
