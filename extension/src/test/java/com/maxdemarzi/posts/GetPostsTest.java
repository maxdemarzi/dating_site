package com.maxdemarzi.posts;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetPostsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetPosts() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetPostsWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts?username2=jexp").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldGetPostsLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetPostsSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts?since=2018-07-19T17:13:00Z").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Max De Marzi'," +
                    "time: datetime('2018-07-19T11:41:23Z')," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "time: datetime('2018-07-19T19:41:23Z')," +
                    "password: 'tunafish'})" +
            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: datetime('2018-07-19T17:12:56Z') })" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: datetime('2018-07-19T17:38:57Z') })" +
            "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z') }]->(post1)" +
            "CREATE (jexp)-[:HIGH_FIVED {time: datetime('2018-07-19T18:33:51Z')}]->(post1)" +
            "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:38:57Z') }]->(post2)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("status", "How are you!");
            put("time", "2018-07-19T17:38:57Z");
            put("name", "Max De Marzi");
            put("username", "maxdemarzi");
            put("hash", "hash");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 0);
            put("low_fives", 0);
        }});
        add(new HashMap<String, Object>() {{
            put("status", "Hello World!");
            put("time", "2018-07-19T17:12:56Z");
            put("name", "Max De Marzi");
            put("username", "maxdemarzi");
            put("hash", "hash");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 1);
            put("low_fives", 0);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("status", "How are you!");
            put("time", "2018-07-19T17:38:57Z");
            put("name", "Max De Marzi");
            put("username", "maxdemarzi");
            put("hash", "hash");
            put("high_fived", false);
            put("low_fived", false);
            put("high_fives", 0);
            put("low_fives", 0);

        }});
        add(new HashMap<String, Object>() {{
            put("status", "Hello World!");
            put("time", "2018-07-19T17:12:56Z");
            put("name", "Max De Marzi");
            put("username", "maxdemarzi");
            put("hash", "hash");
            put("high_fived", true);
            put("low_fived", false);
            put("high_fives", 1);
            put("low_fives", 0);
        }});
    }};
}
