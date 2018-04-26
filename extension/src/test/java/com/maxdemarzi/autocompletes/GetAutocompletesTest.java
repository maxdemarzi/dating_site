package com.maxdemarzi.autocompletes;

import com.maxdemarzi.autocomplete.AutoCompletes;
import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetAutocompletesTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", AutoCompletes.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldAutocompleteThings() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/autocompletes/Thing/lowercase_name/neo").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldAutocompleteOneThing() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/autocompletes/Thing/lowercase_name/java").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldNotAutocompleteBadLabel() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/autocompletes/gnihT/lowercase_name/java").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Label not Valid.", actual.get("error"));
    }

    @Test
    public void shouldNotAutocompleteBadProperty() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/autocompletes/User/password/java").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Property not Valid.", actual.get("error"));
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
                    "CREATE (neo4j:Thing {name:'Neo4j', lowercase_name:'neo4j'})" +
                    "CREATE (neo5j:Thing {name:'Neo5j', lowercase_name:'neo5j'})" +
                    "CREATE (java:Thing {name:'Java', lowercase_name:'java'})";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("name", "Neo4j");
            put("id", 3);
        }});
        add(new HashMap<String, Object>() {{
            put("name", "Neo5j");
            put("id", 4);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("name", "Java");
            put("id", 5);
        }});
    }};
}
