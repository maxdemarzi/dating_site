package com.maxdemarzi.seed;

import org.junit.*;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

public class SeedTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withExtension("/v1", Attributes.class);

    @Test
    public void shouldSeedAttributes() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/seed/attributes").toString());
        String actual  = response.content();
        Assert.assertEquals("Seeded Attributes", actual);
    }


}
