package com.maxdemarzi.schema;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

import static com.maxdemarzi.schema.Properties.*;

@Path("/schema")
public class Schema {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        @POST
        @Path("/create")
        public Response create(@Context GraphDatabaseService db) throws IOException {
            ArrayList<String> results = new ArrayList<>();
            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.Attribute).iterator().hasNext()) {
                    schema.constraintFor(Labels.Attribute)
                            .assertPropertyIsUnique(LOWERCASE_NAME)
                            .create();
                    schema.constraintFor(Labels.Attribute)
                            .assertPropertyIsUnique(NAME)
                            .create();
                    tx.success();
                    results.add("(:Attribute {lowercase_name}) constraint created");
                    results.add("(:Attribute {name}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.User).iterator().hasNext()) {
                    schema.constraintFor(Labels.User)
                            .assertPropertyIsUnique(USERNAME)
                            .create();
                    schema.constraintFor(Labels.User)
                            .assertPropertyIsUnique(EMAIL)
                            .create();
                    tx.success();
                    results.add("(:User {username}) constraint created");
                    results.add("(:User {email}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.City).iterator().hasNext()) {
                    schema.indexFor(Labels.City)
                            .on(LOCATION)
                            .create();
                    tx.success();
                    results.add("(:City {location}) index created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.Tag).iterator().hasNext()) {
                    schema.constraintFor(Labels.Tag)
                            .assertPropertyIsUnique(NAME)
                            .create();
                    tx.success();
                    results.add("(:Tag {name}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.Thing).iterator().hasNext()) {
                    schema.constraintFor(Labels.Thing)
                            .assertPropertyIsUnique(NAME)
                            .create();
                    tx.success();
                    results.add("(:Thing {name}) constraint created");
                }
            }

            results.add("Schema Created");
            return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
        }
}
