package com.maxdemarzi.things;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class GetThingTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Things.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetThing() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/things/Java").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetThingWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/things/Neo4j?username=maxdemarzi").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }


    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'})" +
            "CREATE (neo4j:Thing {name:'Neo4j'})" +
            "CREATE (java:Thing {name:'Java'})" +
            "CREATE (jexp)-[:LIKES {time: 1490140299}]->(neo4j)" +
            "CREATE (laeg)-[:HATES {time: 1490208700}]->(java)" +
            "CREATE (max)-[:LIKES {time: 1490209300 }]->(neo4j)" +
            "CREATE (max)-[:LIKES {time: 1490209400 }]->(java)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
            put("name", "Java");
            put("likes", 1);
            put("hates", 1);
        }};


    private static final HashMap<String, Object> expected2 = new HashMap<String, Object>() {{
            put("name", "Neo4j");
            put("likes", 2);
            put("hates", 0);
            put("liked", true);
            put("hated", false);
        }};
}
