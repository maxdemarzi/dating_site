package com.maxdemarzi.recommended;

import com.maxdemarzi.schema.Schema;
import com.maxdemarzi.timeline.Timeline;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.*;

public class GetRecommendedTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Recommended.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetRecommended() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/recommended").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
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
                    "hash: '0bd90aeb51d5982062f4f303a62df935', " +
                    "time: datetime('2018-03-15T19:41:23Z')," +
                    "name: 'Michaela Hunger'," +
                    "is: 'woman'," +
                    "is_looking_for: ['man']," +
                    "password: 'tunafish'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'lucy@neo4j.com', " +
                    "hash: '0bd90aeb51d5982062f4f303a62df935', " +
                    "time: datetime('2018-03-15T19:41:23Z')," +
                    "name: 'Lucy Gannon'," +
                    "is: 'woman'," +
                    "is_looking_for: ['man', 'woman', 'complicated']," +
                    "password: 'cuddlefish'})" +
                    "CREATE (chicago:City {name:'Chicago', full_name:'Chicago, IL, USA'})" +
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
                    "CREATE (max)-[:HIGH_FIVED {time: datetime('2018-05-01T12:00:01Z')}]->(post1)" +
                    "CREATE (fat:Attribute {name:'Fat'})" +
                    "CREATE (bald:Attribute {name:'Bald'})" +
                    "CREATE (rich:Attribute {name:'Rich'})" +
                    "CREATE (jexp)-[:HAS {time: datetime('2018-07-19T17:12:56Z') }]->(fat)" +
                    "CREATE (jexp)-[:WANTS {time: datetime('2018-07-19T17:12:56Z') }]->(rich)" +
                    "CREATE (jexp)-[:WANTS {time: datetime('2018-07-19T17:12:56Z') }]->(bald)" +
                    "CREATE (laeg)-[:WANTS {time: datetime('2018-07-19T17:38:57Z')}]->(bald)" +
                    "CREATE (laeg)-[:HAS {time: datetime('2018-07-19T17:38:57Z')}]->(rich)" +
                    "CREATE (max)-[:HAS {time: datetime('2018-07-19T18:33:51Z') }]->(fat)" +
                    "CREATE (max)-[:HAS {time: datetime('2018-07-19T19:41:23Z') }]->(bald)" +
                    "CREATE (max)-[:WANTS {time: datetime('2018-07-19T20:11:14Z') }]->(rich)" +
                    "CREATE (neo4j:Thing {name:'Neo4j'})" +
                    "CREATE (java:Thing {name:'Java'})" +
                    "CREATE (max)-[:LIKES {time: datetime('2018-07-19T12:11:46Z') }]->(neo4j)" +
                    "CREATE (max)-[:LIKES {time: datetime('2018-07-19T15:46:23Z') }]->(java)" +
                    "CREATE (jexp)-[:LIKES {time: datetime('2018-07-19T17:12:56Z') }]->(neo4j)" +
                    "CREATE (jexp)-[:HATES {time: datetime('2018-07-19T17:38:57Z') }]->(java)" +
                    "CREATE (laeg)-[:LIKES {time: datetime('2018-07-19T18:33:51Z') }]->(neo4j)" +
                    "CREATE (laeg)-[:LIKES {time: datetime('2018-07-19T19:41:23Z') }]->(java)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Lucy Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("is_looking_for", new ArrayList<String>() {{ add("man"); add("woman"); add("complicated"); }});
            put("is", "woman");
            put("city", "Chicago, IL, USA");
            put("posts", 1);
            put("likes", 2);
            put("hates", 0);
            put("wants", 1);
            put("has", 1);
            put("high_fives", 0);
            put("low_fives", 1);
            put("time", "2018-03-15T19:41:23Z");
            put("points", 2.5);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michaela Hunger");
            put("is_looking_for", new ArrayList<String>() {{ add("man"); }});
            put("is", "woman");
            put("city", "Chicago, IL, USA");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("posts", 1);
            put("likes", 1);
            put("hates", 1);
            put("wants", 2);
            put("has", 1);
            put("high_fives", 0);
            put("low_fives", 0);
            put("time", "2018-03-15T19:41:23Z");
            put("points", 0.33333333333333326);
        }});
    }};
}
