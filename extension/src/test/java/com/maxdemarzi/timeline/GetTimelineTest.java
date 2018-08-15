package com.maxdemarzi.timeline;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetTimelineTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Timeline.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetTimeline() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?since=2018-05-02T11:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetTimelineLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?limit=1&since=2018-05-02T11:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetTimelineSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?since=2018-05-02T11:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetTimelineWithDistance() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?distance=15000&since=2018-05-02T11:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }
    @Test
    public void shouldGetTimelineLocation() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?city=Chicago&state=Illinois&since=2018-05-02T11:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetTimelineCompetition() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?competition=true&since=2018-05-02T11:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(competition, actual);
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Max De Marzi'," +
                    "is: 'man'," +
                    "is_looking_for: ['woman']," +
                    "time: datetime('2018-03-15T19:41:23Z')," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michaela@neo4j.com', " +
                    "hash: 'hash', " +
                    "time: datetime('2018-03-15T19:41:23Z')," +
                    "name: 'Michaela Hunger'," +
                    "is: 'woman'," +
                    "is_looking_for: ['man']," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "hash: 'hash', " +
                    "time: datetime('2018-03-15T19:41:23Z')," +
                    "name: 'Luke Gannon'," +
                    "is: 'man'," +
                    "is_looking_for: ['man', 'woman', 'complicated']," +
                    "password: 'cuddlefish'})" +
            "CREATE (chicago:City {name:'Chicago'})" +
            "CREATE (illinois:State {name:'Illinois'})" +
            "CREATE (chicago)-[:IN_LOCATION]->(illinois)" +
            "CREATE (max)-[:IN_LOCATION]->(chicago)" +
            "CREATE (laeg)-[:IN_LOCATION]->(chicago)" +
            "CREATE (jexp)-[:IN_LOCATION]->(chicago)" +
            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: datetime('2018-05-01T12:00:01Z')})" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: datetime('2018-05-01T13:00:01Z')})" +
            "CREATE (post3:Post {status:'Doing fine!', " +
                    "time: datetime('2018-04-30T12:00:01Z')})" +
            "CREATE (jexp)-[:POSTED_ON_2018_05_01 {time: datetime('2018-05-01T12:00:01Z')}]->(post1)" +
            "CREATE (laeg)-[:POSTED_ON_2018_05_01 {time: datetime('2018-05-01T13:00:01Z')}]->(post2)" +
            "CREATE (max)-[:POSTED_ON_2018_04_30 {time: datetime('2018-04-30T12:00:01Z')}]->(post3)"  +
            "CREATE (laeg)-[:LOW_FIVED {time: datetime('2018-05-01T12:00:01Z')}]->(post1)" +
            "CREATE (max)-[:HIGH_FIVED {time: datetime('2018-05-01T12:00:01Z')}]->(post1)" ;

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michaela Hunger");
            put("hash", "hash");
            put("status", "Hello World!");
            put("time", "2018-05-01T12:00:01Z");
            put("high_fives", 1);
            put("low_fives", 1);
            put("high_fived", true);
            put("low_fived", false);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> competition = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "hash");
            put("status", "How are you!");
            put("time", "2018-05-01T13:00:01Z");
            put("high_fives", 0);
            put("low_fives", 0);
            put("high_fived", false);
            put("low_fived", false);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "hash");
            put("status", "Doing fine!");
            put("time", "2018-04-30T12:00:01Z");
            put("high_fives", 0);
            put("low_fives", 0);
            put("high_fived", false);
            put("low_fived", false);
        }});
    }};

}
