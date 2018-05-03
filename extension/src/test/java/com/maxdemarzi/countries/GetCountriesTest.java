package com.maxdemarzi.countries;

import com.maxdemarzi.schema.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetCountriesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Countries.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldGetCountries() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/countries").toString());
        ArrayList<HashMap<String, Object>> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final String FIXTURE =
            "CREATE (us:Country {name:'US', code:'C1'})" +
            "CREATE (uk:Country {name:'UK', code:'C2'})" +
            "CREATE (ug:Country {name:'UG', code:'C3'})";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("code", "C1");
            put("name", "US");
        }});
        add(new HashMap<String, Object>() {{
            put("code", "C2");
            put("name", "UK");
        }});
        add(new HashMap<String, Object>() {{
            put("code", "C3");
            put("name", "UG");
        }});

    }};
}
