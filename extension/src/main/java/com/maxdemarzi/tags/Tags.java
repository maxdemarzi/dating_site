package com.maxdemarzi.tags;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.maxdemarzi.CustomObjectMapper;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.schema.Properties.COUNT;
import static com.maxdemarzi.schema.Properties.HASH;
import static com.maxdemarzi.schema.Properties.HIGH_FIVED;
import static com.maxdemarzi.schema.Properties.HIGH_FIVES;
import static com.maxdemarzi.schema.Properties.LOW_FIVED;
import static com.maxdemarzi.schema.Properties.LOW_FIVES;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.STATUS;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static java.util.Collections.reverseOrder;

@Path("/tags")
public class Tags {

    private static final Pattern hashtagPattern = Pattern.compile("#(\\S+)");

    private static final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private static final Comparator<Map<String, Object>> timedComparator = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());
    private static GraphDatabaseService db;

    public Tags(@Context GraphDatabaseService graphDatabaseService) {
        db = graphDatabaseService;
    }

    // Cache
    public static LoadingCache<String, List<Map<String, Object>>> trends = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(Tags::getTrends);

    private static List<Map<String, Object>> getTrends(String key) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now(utc);
        RelationshipType tagged = RelationshipType.withName("TAGGED_ON_" +
                dateTime.format(dateFormatter));
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> tags = db.findNodes(Labels.Tag);
            while (tags.hasNext()) {
                Node tag = tags.next();
                int taggings = tag.getDegree(tagged, Direction.INCOMING);
                if ( taggings > 0) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(NAME, tag.getProperty(NAME));
                    result.put(COUNT, taggings);
                    results.add(result);
                }
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Integer) m.get(COUNT), reverseOrder()));
        return results.subList(0, Math.min(results.size(), 10));
    }

    @GET
    @Path("/{hashtag}")
    public Response getTags(@PathParam("hashtag") final String hashtag,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final String since,
                             @QueryParam("username") final String username,
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
            Node user;
            HashSet<Node> highFived = new HashSet<>();
            HashSet<Node> lowFived = new HashSet<>();
            if (username != null) {
                user = Users.findUser(username, db);
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HIGH_FIVED)) {
                    highFived.add(r1.getEndNode());
                }
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LOW_FIVED)) {
                    lowFived.add(r1.getEndNode());
                }
            }

            Node tag = db.findNode(Labels.Tag, NAME, hashtag.toLowerCase());
            if (tag != null) {
                ZonedDateTime earliestTag = (ZonedDateTime)tag.getProperty(TIME);

                int count = 0;
                while (count < limit && (dateTime.isAfter(earliestTag))) {
                    RelationshipType relType = RelationshipType.withName("TAGGED_ON_" +
                            dateTime.format(dateFormatter));

                    for (Relationship r1 : tag.getRelationships(Direction.INCOMING, relType)) {
                        Node post = r1.getStartNode();
                        Map<String, Object> result = post.getAllProperties();
                        ZonedDateTime time = (ZonedDateTime) result.get("time");

                        if (count < limit && time.isBefore(latest) ) {
                            Node author = getAuthor(post, time);
                            Map userProperties = author.getAllProperties();
                            result.put(USERNAME, userProperties.get(USERNAME));
                            result.put(NAME, userProperties.get(NAME));
                            result.put(HASH, userProperties.get(HASH));
                            result.put(HIGH_FIVED, highFived.contains(post));
                            result.put(LOW_FIVED, lowFived.contains(post));
                            result.put(HIGH_FIVES, post.getDegree(RelationshipTypes.HIGH_FIVED ,Direction.INCOMING));
                            result.put(LOW_FIVES, post.getDegree(RelationshipTypes.LOW_FIVED ,Direction.INCOMING));
                            results.add(result);
                            count++;
                        }
                    }
                    dateTime = dateTime.minusDays(1);
                }
                tx.success();
                results.sort(timedComparator);
            } else {
                throw TagExceptions.tagNotFound;
            }
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static void createTags(Node post, HashMap<String, Object> input, ZonedDateTime dateTime, GraphDatabaseService db) {
        Matcher mat = hashtagPattern.matcher(((String)input.get(STATUS)).toLowerCase());
        for (Relationship r1 : post.getRelationships(Direction.OUTGOING, RelationshipType.withName("TAGGED_ON_" +
                dateTime.format(dateFormatter)))) {
            r1.delete();
        }
        Set<Node> tagged = new HashSet<>();
        while (mat.find()) {
            String tag = mat.group(1);
            Node hashtag = db.findNode(Labels.Tag, NAME, tag);
            if (hashtag == null) {
                hashtag = db.createNode(Labels.Tag);
                hashtag.setProperty(NAME, tag);
                hashtag.setProperty(TIME, dateTime);
            }
            if (!tagged.contains(hashtag)) {
                post.createRelationshipTo(hashtag, RelationshipType.withName("TAGGED_ON_" +
                        dateTime.format(dateFormatter)));
                tagged.add(hashtag);
            }
        }
    }

    @GET
    public Response getTrends(@Context GraphDatabaseService db) throws IOException {
        List<Map<String, Object>> results;
        try (Transaction tx = db.beginTx()) {
            results = trends.get("trends");
            tx.success();
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }
}
