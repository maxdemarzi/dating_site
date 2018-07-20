package com.maxdemarzi.mentions;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetMentionsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Mentions.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetMentions() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetMentionsWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions?username2=maxdemarzi").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldGetMentionsLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetMentionsSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions?since=2018-07-19T17:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "time: datetime('2018-07-15T03:43:22Z')," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "time: datetime('2018-07-15T03:43:22Z')," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'})" +
            "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "name: 'Mark Needham'," +
                    "password: 'jellyfish'})" +
            "CREATE (post1:Post {status:'Hello @jexp', " +
                    "time: datetime('2018-07-19T17:12:56Z')})" +
            "CREATE (post2:Post {status:'Hi @jexp', " +
                    "time: datetime('2018-07-19T17:38:57Z')})" +
            "CREATE (post3:Post {status:'Stalking @jexp', " +
                    "time: datetime('2018-07-19T19:41:23Z')})" +

            "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z')}]->(post1)" +
            "CREATE (laeg)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:38:57Z')}]->(post2)" +
            "CREATE (mark)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T19:41:23Z')}]->(post3)" +
            "CREATE(post1)-[:MENTIONED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z')}]->(jexp)" +
            "CREATE(post2)-[:MENTIONED_ON_2018_07_19 {time: datetime('2018-07-19T17:38:57Z')}]->(jexp)" +
            "CREATE(post3)-[:MENTIONED_ON_2018_07_19 {time: datetime('2018-07-19T19:41:23Z')}]->(jexp)" +
            "CREATE (max)-[:LOW_FIVED {time: datetime('2018-07-19T17:58:57Z') }]->(post2)" +
            "CREATE (jexp)-[:BLOCKS {time: datetime('2018-07-19T19:44:23Z') }]->(mark)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hi @jexp");
            put("time", "2018-07-19T17:38:57Z");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 0);
            put("low_fives", 1);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("time", "2018-07-19T17:12:56Z");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 0);
            put("low_fives", 0);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hi @jexp");
            put("time", "2018-07-19T17:38:57Z");
            put("high_fived", false);
            put("low_fived", true);
            put("high_fives", 0);
            put("low_fives", 1);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("time", "2018-07-19T17:12:56Z");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 0);
            put("low_fives", 0);
        }});
    }};
}
