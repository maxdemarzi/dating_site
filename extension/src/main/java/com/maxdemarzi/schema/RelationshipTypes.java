package com.maxdemarzi.schema;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    ADDED_TO,
    BLOCKS,
    HAS,
    HATES,
    HIGH_FIVED,
    IN_LOCATION,
    IN_TIMEZONE,
    LIKES,
    LOW_FIVED,
    PART_OF,
    REPLIED_TO,
    WANTS
}