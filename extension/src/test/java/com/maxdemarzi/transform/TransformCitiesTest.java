package com.maxdemarzi.transform;

import com.maxdemarzi.schema.Schema;
import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.LOWERCASE_FULL_NAME;

public class TransformCitiesTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Cities.class)
            .withExtension("/v1", com.maxdemarzi.cities.Cities.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldTransformCities() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/transform/cities").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/cities/1234").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final String FIXTURE =
            "CREATE (durres:City {name:'Durrës', geoname_id:'1234'})" +
            "CREATE (durresit:State {name:'Qarku i Durresit'})" +
            "CREATE (albania:Country {name:'Albania'})" +
            "CREATE (durres)-[:IN_LOCATION]->(durresit)" +
            "CREATE (durresit)-[:IN_LOCATION]->(albania)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("name", "Durrës");
        put("geoname_id", "1234");
        put(LOWERCASE_FULL_NAME, "durres, qarku i durresit, albania");
        put("live_in", 0);
    }};
}
