package com.maxdemarzi.fives;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Time.utc;

public class GetHighFivesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(String.format(FIXTURE, ZonedDateTime.now(utc), ZonedDateTime.now(utc) ))
            .withExtension("/v1", HighFives.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetHighFives() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/high_fives").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 2);
        Assert.assertEquals(actual.get(0).get("high_fives"), 1);
        Assert.assertEquals(actual.get(0).get("username2"), "laexample");
    }


    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "timezone: 'America/Chicago'," +
                    "name: 'Max De Marzi'," +
                    "hash: '12', " +
                    "password: 'swordfish'})" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "timezone: 'America/Chicago'," +
                    "name: 'Michael Hunger'," +
                    "hash: '34', " +
                    "password: 'tunafish'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "timezone: 'America/Chicago'," +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '1234', " +
                    "password: 'cuddlefish'})" +
                    "CREATE (post1:Post {status:'Hello World!', " +
                    "time: datetime('2018-07-19T17:12:56Z') })" +
                    "CREATE (post2:Post {status:'How are you!', " +
                    "time: datetime('2018-07-19T18:00:23Z') })" +
                    "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z') }]->(post1)" +
                    "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T18:00:23Z') }]->(post2)" +
                    "CREATE (jexp)-[:HIGH_FIVED {time: datetime('%s')}]->(post1)" +
                    "CREATE (laeg)-[:HIGH_FIVED {time: datetime('%s')}]->(post2)" +
                    "CREATE (chicago:City {name:'Chicago', geoname_id:'1234'})" +
                    "CREATE (illinois:State {name:'Illinois'})" +
                    "CREATE (america:Timezone {name:'America/Chicago'})" +
                    "CREATE (chicago)-[:IN_LOCATION]->(illinois)" +
                    "CREATE (illinois)-[:IN_TIMEZONE]->(america)" +
                    "CREATE (max)-[:IN_LOCATION]->(chicago)" +
                    "CREATE (laeg)-[:IN_LOCATION]->(chicago)" +
                    "CREATE (jexp)-[:IN_LOCATION]->(chicago)";
}
