package com.maxdemarzi.hates;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetHatesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Hates.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetHates() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/hates").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetHatesWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/hates?username2=maxdemarzi").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldGetHatesLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/hates?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetHatesSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/hates?since=1490209400").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
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
            "CREATE (jexp)-[:HATES {time: 1490140299}]->(java)" +
            "CREATE (laeg)-[:HATES {time: 1490208700}]->(java)" +
            "CREATE (max)-[:HATES {time: 1490209300 }]->(neo4j)" +
            "CREATE (max)-[:HATES {time: 1490209400 }]->(java)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("name", "Java");
            put("time", 1490209400);
            put("likes", 0);
            put("hates", 3);
            put("liked", false);
            put("hated", false);
            put("shared", false);
        }});
        add(new HashMap<String, Object>() {{
            put("name", "Neo4j");
            put("time", 1490209300);
            put("likes", 1);
            put("hates", 1);
            put("liked", false);
            put("hated", false);
            put("shared", false);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("name", "Java");
            put("time", 1490140299);
            put("likes", 0);
            put("hates", 3);
            put("liked", false);
            put("hated", true);
            put("shared", true);
        }});
    }};
}
