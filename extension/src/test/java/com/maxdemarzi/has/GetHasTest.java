package com.maxdemarzi.has;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetHasTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Has.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetHas() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/has").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetHasWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/has?username2=maxdemarzi").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldGetHasLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/has?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetHasOffset() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/has?offset=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
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
            "CREATE (fat:Attribute {name:'Fat'})" +
            "CREATE (bald:Attribute {name:'Bald'})" +
            "CREATE (rich:Attribute {name:'Rich'})" +
            "CREATE (jexp)-[:HAS {time: datetime('2018-07-19T17:12:56Z') }]->(fat)" +
            "CREATE (laeg)-[:WANTS {time: datetime('2018-07-19T17:38:57Z')}]->(bald)" +
            "CREATE (max)-[:HAS {time: datetime('2018-07-19T18:33:51Z') }]->(fat)" +
            "CREATE (max)-[:HAS {time: datetime('2018-07-19T19:41:23Z') }]->(bald)" +
            "CREATE (max)-[:WANTS {time: datetime('2018-07-19T20:11:14Z') }]->(rich)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("name", "Bald");
            put("time", "2018-07-19T19:41:23Z");
            put("has", 1);
            put("wants", 1);
            put("have", false);
            put("want", false);
        }});
        add(new HashMap<String, Object>() {{
            put("name", "Fat");
            put("time", "2018-07-19T18:33:51Z");
            put("has", 2);
            put("wants", 0);
            put("want", false);
            put("have", false);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("name", "Fat");
            put("time", "2018-07-19T17:12:56Z");
            put("has", 2);
            put("wants", 0);
            put("want", false);
            put("have", true);
        }});
    }};
}
