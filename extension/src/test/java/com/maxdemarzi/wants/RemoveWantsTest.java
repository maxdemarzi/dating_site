package com.maxdemarzi.wants;

import com.maxdemarzi.schema.Schema;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class RemoveWantsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Wants.class)
            .withExtension("/v1", Schema.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldRemoveWants() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/wants/Fat").toString(), null);
    }

    @Test
    public void shouldRemoveWants2() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/wants/Bald").toString(), null);
    }

    @Test
    public void shouldNotRemoveWantsUserNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/max/wants/Fat").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("User not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveWantsThingNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/wants/Slim").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Attribute not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveWantsNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/markhneedham/wants/Fat").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Not wanting Attribute.", actual.get("error"));
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
                    "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "name: 'Mark Needham'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'jellyfish'})" +
                    "CREATE (fat:Attribute {name:'Fat'})" +
                    "CREATE (bald:Attribute {name:'Bald'})" +
                    "CREATE (jexp)-[:WANTS {time: 1490140299}]->(fat)" +
                    "CREATE (jexp)-[:WANTS {time: 1490140299}]->(bald)" +
                    "CREATE (laeg)-[:HAS {time: 1490208700}]->(bald)" +
                    "CREATE (max)-[:WANTS {time: 1490209300 }]->(fat)" +
                    "CREATE (max)-[:WANTS {time: 1490209400 }]->(bald)";
}
