package com.maxdemarzi.attributes;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.*;

public class GetAttributeTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Attributes.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetAttribute() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/attributes/Bald").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAttributeWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/attributes/Bald?username=maxdemarzi").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }


    @Test
    public void shouldNotGetAttributeNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/attributes/notBald").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Attribute not Found.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(STATUS));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
                    "CREATE (fat:Attribute {name:'Fat'})" +
                    "CREATE (bald:Attribute {name:'Bald'})" +
                    "CREATE (jexp)-[:HAS {time: 1490140299}]->(fat)" +
                    "CREATE (laeg)-[:WANTS {time: 1490208700}]->(bald)" +
                    "CREATE (max)-[:HAS {time: 1490209300 }]->(fat)" +
                    "CREATE (max)-[:HAS {time: 1490209400 }]->(bald)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("name", "Bald");
        put("has", 1);
        put("wants", 1);
    }};

    private static final HashMap<String, Object> expected2 = new HashMap<String, Object>() {{
        put("name", "Bald");
        put("has", 1);
        put("wants", 1);
        put("have", true);
        put("want", false);
    }};

}
