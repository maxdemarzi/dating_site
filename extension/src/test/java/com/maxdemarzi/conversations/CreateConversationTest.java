package com.maxdemarzi.conversations;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.schema.Properties.STATUS;

public class CreateConversationTest {


    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(String.format(FIXTURE, ZonedDateTime.now(utc), ZonedDateTime.now(utc) ))
            .withExtension("/v1", Conversations.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldCreateConversation() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/conversations/jexp").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected.get("status"), actual.get("status"));
        Assert.assertEquals(expected.get("author"), actual.get("author"));
    }

    private static final HashMap input = new HashMap<String, Object>() {{
        put(STATUS, "Hello Michael!");
    }};

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
            put("status", "Hello Michael!");
            put("author", "maxdemarzi");
        }};

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Max De Marzi'," +
                    "timezone: 'America/Chicago'," +
                    "time: datetime('2018-07-15T11:41:23Z')," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "timezone: 'America/Chicago'," +
                    "time: datetime('2018-07-15T19:41:23Z')," +
                    "password: 'tunafish'})" +
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
