package com.maxdemarzi.posts;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.STATUS;

public class UpdatePostTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldUpdatePost() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("PUT", neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts/2018-07-19T17:12:56Z").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final HashMap input = new HashMap<String, Object>() {{
        put(STATUS, "Hello Again!");
    }};

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
            "CREATE (post:Post {status:'Hello World!', " +
                    "time: datetime('2018-07-19T17:12:56Z') })" +
            "CREATE (max)-[:POSTED_ON_2018_07_19]->(post)" +
            "CREATE (lage)-[:LOW_FIVED {time: datetime('2018-07-19T18:33:51Z') }]->(post)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("status", "Hello Again!");
        put("time", "2018-07-19T17:12:56Z");
        put("high_fived", false);
        put("low_fived", false);
        put("high_fives", 0);
        put("low_fives", 1);
    }};
}
