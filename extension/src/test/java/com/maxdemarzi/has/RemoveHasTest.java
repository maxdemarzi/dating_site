package com.maxdemarzi.has;

import com.maxdemarzi.schema.Schema;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class RemoveHasTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Has.class)
            .withExtension("/v1", Schema.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldRemoveHas() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/has/Fat").toString(), null);
    }

    @Test
    public void shouldRemoveHas2() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/has/Bald").toString(), null);
    }

    @Test
    public void shouldNotRemoveHasUserNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/max/has/Fat").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("User not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveHasThingNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/has/Slim").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Attribute not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveHasNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/markhneedham/has/Fat").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Not having Attribute.", actual.get("error"));
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
                    "CREATE (jexp)-[:HAS {time: datetime('2018-07-19T17:12:56Z')}]->(fat)" +
                    "CREATE (jexp)-[:HAS {time: datetime('2018-07-19T17:12:56Z')}]->(bald)" +
                    "CREATE (laeg)-[:WANTS {time: datetime('2018-07-19T17:12:56Z')}]->(bald)" +
                    "CREATE (max)-[:HAS {time: datetime('2018-07-19T17:12:56Z') }]->(fat)" +
                    "CREATE (max)-[:HAS {time: datetime('2018-07-19T17:12:56Z') }]->(bald)";
}
