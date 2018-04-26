package com.maxdemarzi.autocomplete;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.StringSearchMode;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.schema.Properties.ID;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.USERNAME;

@Path("/autocompletes")
public class AutoCompletes {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HashSet<String> labels = new HashSet<String>() {{
        add("Attribute");
        add("User");
        add("Tag");
        add("Thing");
    }};

    private static final HashSet<String> properties = new HashSet<String>() {{
        add("username");
        add("lowercase_name");
    }};

    @GET
    @Path("/{label}/{property}/{query}")
    public Response getQuery(@PathParam("label") final String label,
                             @PathParam("property") final String property,
                             @PathParam("query") final String query,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {

            if (!labels.contains(label)) {
                throw AutoCompleteExceptions.labelNotValid;
            }
            if (!properties.contains(property)) {
                throw AutoCompleteExceptions.propertyNotValid;
            }

            ResourceIterator<Node> nodes = db.findNodes(Label.label(label), property, query, StringSearchMode.PREFIX);

            nodes.forEachRemaining(node -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put(ID, node.getId());
                Map<String, Object> properties = node.getAllProperties();
                if (properties.containsKey(USERNAME)) {
                    map.put(USERNAME, properties.get(USERNAME));
                }
                if (properties.containsKey(NAME)) {
                    map.put(NAME, properties.get(NAME));
                }
                results.add(map);
            });

            tx.success();
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }
}
