package com.maxdemarzi.conversations;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetConversationsTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Conversations.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetConversations() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/conversations").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("status", "How are you!");
            put("time", "2018-07-19T17:38:57Z");
            put("author", "jexp");
            put("name", "Michael Hunger");
            put("username", "jexp");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
        }});
    }};

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Max De Marzi'," +
                    "time: datetime('2018-07-15T11:41:23Z')," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "time: datetime('2018-07-15T19:41:23Z')," +
                    "password: 'tunafish'})" +
            "CREATE (conversation:Conversation) " +
            "CREATE (m1:Message {status:'Hello!', " +
                    "time: datetime('2018-07-19T17:12:56Z'), author:'maxdemarzi' })" +
            "CREATE (m2:Message {status:'How are you!', " +
                    "time: datetime('2018-07-19T17:38:57Z') , author:'jexp'})" +
            "CREATE (max)-[:PART_OF]->(conversation)" +
            "CREATE (jexp)-[:PART_OF]->(conversation)" +
            "CREATE (m1)-[:ADDED_TO]->(conversation)" +
            "CREATE (m2)-[:ADDED_TO]->(conversation)";
}
