package com.maxdemarzi.wants;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.STATUS;
import static com.maxdemarzi.schema.Properties.TIME;

public class CreateWantsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Wants.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldCreateWants() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/wants/Bald").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotCreateWantsTwice() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/wants/Bald").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/wants/Bald").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Already wants Attribute.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
            "CREATE (bald:Attribute {name:'Bald'})";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("name", "Bald");
        put("has", 0);
        put("wants", 1);
        put("have", false);
        put("want", true);
    }};
}
