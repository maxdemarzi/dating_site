package com.maxdemarzi.users;

import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class GetProfileTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Users.class);

    @Test
    public void shouldGetProfile() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/profile").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetProfileSecondUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/profile?username2=jexp").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'})" +
            "CREATE (stefan:User {username:'darthvader42', " +
                    "email: 'stefan@neo4j.com', " +
                    "name: 'Stefan Armbruster'," +
                    "password: 'catfish'})" +
            "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "name: 'Mark Needham'," +
                    "password: 'jellyfish'})" +
            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: 1490208700})" +
            "CREATE (post3:Post {status:'Doing fine thanks!', " +
                    "time: 1490290191})" +
            "CREATE (city:City {full_name:'Chicago'})" +
            "CREATE (jexp)-[:POSTED_ON_2017_03_21]->(post1)" +
            "CREATE (max)-[:POSTED_ON_2017_03_22]->(post2)" +
            "CREATE (max)-[:POSTED_ON_2017_03_23]->(post3)" +
            "CREATE (neo4j:Thing {name:'Neo4j'})" +
            "CREATE (java:Thing {name:'Java'})" +
            "CREATE (jexp)-[:LIKES {time: 1490140299}]->(neo4j)" +
            "CREATE (laeg)-[:HATES {time: 1490208700}]->(java)" +
            "CREATE (max)-[:LIKES {time: 1490209300 }]->(neo4j)" +
            "CREATE (max)-[:HATES {time: 1490209400 }]->(java)" +
            "CREATE (fat:Attribute {name:'Fat'})" +
            "CREATE (bald:Attribute {name:'Bald'})" +
            "CREATE (jexp)-[:WANTS {time: 1490140299}]->(fat)" +
            "CREATE (laeg)-[:HAS {time: 1490208700}]->(bald)" +
            "CREATE (max)-[:WANTS {time: 1490209300 }]->(fat)" +
            "CREATE (max)-[:HAS {time: 1490209400 }]->(bald)" +
            "CREATE (max)-[:IN_LOCATION]->(city)";

    private static final HashMap expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("city", "Chicago");
        put("posts", 2);
        put("likes", 1);
        put("hates", 1);
        put("has", 1);
        put("wants", 1);
        put("high_fives", 0);
        put("low_fives", 0);
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
    }};

    private static final HashMap expected2 = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("city", "Chicago");
        put("posts", 2);
        put("likes", 1);
        put("has", 1);
        put("wants", 1);
        put("hates", 1);
        put("have", 1);
        put("want", 1);
        put("high_fives", 0);
        put("low_fives", 0);
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
    }};
}
