package com.maxdemarzi.transform;

import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.Normalizer;

import static com.maxdemarzi.schema.Properties.LOWERCASE_FULL_NAME;
import static com.maxdemarzi.schema.Properties.NAME;

@Path("/transform/cities")
public class Cities {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @POST
    public Response transform(@Context GraphDatabaseService db) throws IOException {
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> countries = db.findNodes(Labels.Country);
            while (countries.hasNext()) {
                Node country = countries.next();
                String countryName = (String)country.getProperty(NAME);
                for (Relationship rel : country.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                    Node state = rel.getStartNode();
                    String stateName = (String)state.getProperty(NAME);
                    for (Relationship rel2 : state.getRelationships(Direction.INCOMING, RelationshipTypes.IN_LOCATION)) {
                        Node city = rel2.getStartNode();
                        String cityName = (String)city.getProperty(NAME);
                        city.setProperty(LOWERCASE_FULL_NAME,
                                Normalizer.normalize(cityName + ", " + stateName + ", " + countryName, Normalizer.Form.NFD)
                                        .replaceAll("[^\\p{ASCII}]", "").toLowerCase());
                    }
                }
            }

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString("Transformed Cities")).build();
    }

}
