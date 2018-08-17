package com.maxdemarzi.fives;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.time.ZonedDateTime;
import java.util.HashMap;

import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.*;
import static com.maxdemarzi.schema.Properties.STATUS;
import static com.maxdemarzi.schema.Properties.TIME;

public class CreateHighFiveTest {

    //private static final String dateRelType = ZonedDateTime.now(utc).format(dateFormatter));
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(String.format(FIXTURE, ZonedDateTime.now(utc), ZonedDateTime.now(utc), ZonedDateTime.now(utc), ZonedDateTime.now(utc), ZonedDateTime.now(utc) ))
            .withExtension("/v1", HighFives.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldCreateHighFive() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/jexp/high_fives/maxdemarzi/2018-07-19T17:12:56Z").toString());
        HashMap actual  = response.content();
        Assert.assertTrue((boolean)actual.get(HIGH_FIVED));
        Assert.assertEquals(actual.get(HIGH_FIVES), 1);
    }

    @Test
    public void shouldNotCreateHighFiveAlreadyFived() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/jexp/high_fives/maxdemarzi/2018-07-19T18:00:23Z").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Already high fived Post.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    @Test
    public void shouldNotCreateHighFiveOverLimit() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/laexample/high_fives/maxdemarzi/2018-07-19T18:00:23Z").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(403, response.status());
        Assert.assertEquals("Over high five Limit.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'})" +
            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: datetime('2018-07-19T17:12:56Z') })" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: datetime('2018-07-19T18:00:23Z') })" +
            "CREATE (post3:Post {status:'Dummy Post 1!', " +
                    "time: datetime('2018-07-19T19:01:00Z') })" +
            "CREATE (post4:Post {status:'Dummy Post 2!', " +
                    "time: datetime('2018-07-19T19:02:00Z') })" +
            "CREATE (post5:Post {status:'Dummy Post 3!', " +
                    "time: datetime('2018-07-19T19:03:00Z') })" +
            "CREATE (post6:Post {status:'Dummy Post 4!', " +
                    "time: datetime('2018-07-19T19:04:00Z') })" +
            "CREATE (post7:Post {status:'Dummy Post 5!', " +
                    "time: datetime('2018-07-19T19:05:00Z') })" +
            "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T17:12:56Z') }]->(post1)" +
            "CREATE (max)-[:POSTED_ON_2018_07_19 {time: datetime('2018-07-19T18:00:23Z') }]->(post2)" +
            "CREATE (jexp)-[:HIGH_FIVED {time: datetime('2018-07-19T18:33:51Z')}]->(post2)" +
            "CREATE (laeg)-[:HIGH_FIVED {time: datetime('%s')}]->(post3)" +
            "CREATE (laeg)-[:HIGH_FIVED {time: datetime('%s')}]->(post4)" +
            "CREATE (laeg)-[:HIGH_FIVED {time: datetime('%s')}]->(post5)" +
            "CREATE (laeg)-[:HIGH_FIVED {time: datetime('%s')}]->(post6)" +
            "CREATE (laeg)-[:HIGH_FIVED {time: datetime('%s')}]->(post7)" +
            "CREATE (chicago:City {name:'Chicago', geoname_id:'1234'})" +
            "CREATE (illinois:State {name:'Illinois'})" +
            "CREATE (america:Timezone {name:'America/Chicago'})" +
            "CREATE (chicago)-[:IN_LOCATION]->(illinois)" +
            "CREATE (illinois)-[:IN_TIMEZONE]->(america)" +
            "CREATE (max)-[:IN_LOCATION]->(chicago)" +
            "CREATE (laeg)-[:IN_LOCATION]->(chicago)" +
            "CREATE (jexp)-[:IN_LOCATION]->(chicago)";


}
