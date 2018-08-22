package com.maxdemarzi.cities;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class GetCityTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Cities.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetCityByGeonameId() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/cities/1234").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetCityByCityAndState() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/cities/Chicago/Illinois").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void shouldNotGetCityNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/cities/4321").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("City not Found.", actual.get("error"));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Max De Marzi'," +
                    "is: 'man'," +
                    "is_looking_for: ['woman']," +
                    "time: 1525048800," +
                    "password: 'swordfish'})" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michaela@neo4j.com', " +
                    "hash: 'hash', " +
                    "time: 1525048800," +
                    "name: 'Michaela Hunger'," +
                    "is: 'woman'," +
                    "is_looking_for: ['man']," +
                    "password: 'tunafish'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "hash: 'hash', " +
                    "time: 1525048800," +
                    "name: 'Luke Gannon'," +
                    "is: 'man'," +
                    "is_looking_for: ['man', 'woman', 'complicated']," +
                    "password: 'cuddlefish'})" +
            "CREATE (chicago:City {name:'Chicago', geoname_id:'1234'})" +
            "CREATE (illinois:State {name:'Illinois'})" +
            "CREATE (chicago)-[:IN_LOCATION]->(illinois)" +
            "CREATE (max)-[:IN_LOCATION]->(chicago)" +
            "CREATE (laeg)-[:IN_LOCATION]->(chicago)" +
            "CREATE (jexp)-[:IN_LOCATION]->(chicago)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("name", "Chicago");
        put("geoname_id", "1234");
        put("live_in", 3);
    }};

}
