package com.maxdemarzi.states;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetStateCitiesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", States.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetStateCities() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/states/CA/cities").toString());
        ArrayList<HashMap<String, Object>> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotGetStateCitiesStateNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/states/X1/cities").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("State not Found.", actual.get("error"));
    }

    private static final String FIXTURE =
            "CREATE (us:Country {name:'US', code:'C1'})" +
            "CREATE (uk:Country {name:'UK', code:'C2'})" +
            "CREATE (ug:Country {name:'UG', code:'C3'})" +
            "CREATE (chicago:City {name:'Chicago', code:'CHI'})" +
            "CREATE (illinois:State {name:'Illinois', code:'IL'})" +
            "CREATE (sanmateo:City {name:'San Mateo', code:'SAMA'})" +
            "CREATE (sanfran:City {name:'San Francisco', code:'SF'})" +
            "CREATE (california:State {name:'California', code:'CA'})" +
            "CREATE (chicago)-[:IN_LOCATION]->(illinois)" +
            "CREATE (illinois)-[:IN_LOCATION]->(us)" +
            "CREATE (sanmateo)-[:IN_LOCATION]->(california)" +
            "CREATE (sanfran)-[:IN_LOCATION]->(california)" +
            "CREATE (california)-[:IN_LOCATION]->(us)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("code", "SF");
            put("name", "San Francisco");
        }});
        add(new HashMap<String, Object>() {{
            put("code", "SAMA");
            put("name", "San Mateo");
        }});

    }};
}
