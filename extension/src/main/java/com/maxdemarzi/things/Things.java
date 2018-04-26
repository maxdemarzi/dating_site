package com.maxdemarzi.things;

import com.maxdemarzi.schema.Labels;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import javax.ws.rs.core.Context;

import static com.maxdemarzi.schema.Properties.NAME;

public class Things {

    public static Node findThing(String name, @Context GraphDatabaseService db) {
        if (name == null) { return null; }
        Node thing = db.findNode(Labels.Thing, NAME, name);
        if (thing == null) { throw ThingExceptions.thingNotFound; }
        return thing;
    }
}
