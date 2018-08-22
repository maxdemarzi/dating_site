package com.maxdemarzi.hates;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.STATUS;
import static com.maxdemarzi.schema.Properties.TIME;

public class CreateHatesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Hates.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldCreateHates() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/hates/Neo4j").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotCreateHatesTwice() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/hates/Neo4j").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/hates/Neo4j").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Already hates Thing.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
            "CREATE (neo4j:Thing {name:'Neo4j'})";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("name", "Neo4j");
        put("hates", 1);
        put("likes", 0);
        put("liked", false);
        put("hated", true);
    }};
}
