package com.maxdemarzi.blocks;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetBlocksTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Blocks.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetBlocks() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetBlocksLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetBlocksSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks?since=2018-05-11T12:00:01Z").toString());
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
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'})" +
            "CREATE (city:City {full_name:'Chicago'})" +
            "CREATE (max)-[:IN_LOCATION]->(city)" +
            "CREATE (laeg)-[:IN_LOCATION]->(city)" +
            "CREATE (jexp)-[:IN_LOCATION]->(city)" +
            "CREATE (max)-[:BLOCKS {time: datetime('2018-05-01T12:00:01Z')}]->(jexp)" +
            "CREATE (max)-[:BLOCKS {time: datetime('2018-06-01T12:00:01Z')}]->(laeg)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("posts", 0);
            put("likes", 0);
            put("hates", 0);
            put("wants", 0);
            put("has", 0);
            put("high_fives", 0);
            put("low_fives", 0);
            put("time", "2018-06-01T12:00:01Z");
            put("city", "Chicago");
        }});
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("posts", 0);
            put("likes", 0);
            put("hates", 0);
            put("wants", 0);
            put("has", 0);
            put("high_fives", 0);
            put("low_fives", 0);
            put("time", "2018-05-01T12:00:01Z");
            put("city", "Chicago");
        }});
    }};
}
