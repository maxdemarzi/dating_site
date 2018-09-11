package com.maxdemarzi.tags;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.*;

public class GetTagTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Tags.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetTag() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetTagWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j?username=maxdemarzi").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldGetTagLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetTagSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j?since=2018-07-19T17:38:57Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
    }

    @Test
    public void shouldNotGetTagNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/notneo4j").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Tag Not Found.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(STATUS));
    }

    @Test
    public void shouldGetTags() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        Tags.trends.invalidateAll();
        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(new ArrayList<HashMap>(), actual);
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
                    "CREATE (post1:Post {status:'Hello World! #neo4j', " +
                    "time: datetime('2018-07-19T17:12:56Z')})" +
                    "CREATE (post2:Post {status:'How are you! #neo4j', " +
                    "time: datetime('2018-07-19T17:38:57Z')})" +
                    "CREATE (neo4j:Tag {name:'neo4j', time: datetime('2018-06-01T10:33:11Z')})" +
                    "CREATE (jexp)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z')}]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:38:57Z')}]->(post2)" +
                    "CREATE (max)-[:HIGH_FIVED {time: datetime('2018-07-19T18:33:51Z') }]->(post1)" +
                    "CREATE (max)-[:LOW_FIVED {time: datetime('2018-07-19T19:41:23Z') }]->(post2)" +
                    "CREATE (post1)-[:TAGGED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z') }]->(neo4j)" +
                    "CREATE (post2)-[:TAGGED_ON_2018_07_19 {time: datetime('2018-07-19T17:38:57Z') }]->(neo4j)" ;

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("id", 4);
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "How are you! #neo4j");
            put("time", "2018-07-19T17:38:57Z");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 0);
            put("low_fives", 1);
        }});
        add(new HashMap<String, Object>() {{
            put("id", 3);
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World! #neo4j");
            put("time", "2018-07-19T17:12:56Z");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 1);
            put("low_fives", 0);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("id", 4);
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "How are you! #neo4j");
            put("time", "2018-07-19T17:38:57Z");
            put("high_fived", false);
            put("low_fived", true);
            put("high_fives", 0);
            put("low_fives", 1);

        }});
        add(new HashMap<String, Object>() {{
            put("id", 3);
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World! #neo4j");
            put("time", "2018-07-19T17:12:56Z");
            put("high_fived", true);
            put("low_fived", false);
            put("high_fives", 1);
            put("low_fives", 0);
        }});
    }};
}
