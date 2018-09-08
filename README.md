# Dating Site
Example Dating Site with Neo4j

Follow along with the development at http://maxdemarzi.com

[![Build Status](https://travis-ci.org/maxdemarzi/dating_site.svg?branch=master)](https://travis-ci.org/maxdemarzi/dating_site)

[![Coverage Status](https://coveralls.io/repos/github/maxdemarzi/dating_site/badge.svg?branch=master)](https://coveralls.io/github/maxdemarzi/dating_site?branch=master)


Setup
====

Let's start with Wikipedia to import Things:

	git clone https://github.com/maxdemarzi/graphipedia
	cd graphipedia
	mvn clean package
	cd ..
	wget https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2
	bzip2 -dc enwiki-latest-pages-articles.xml.bz2 | java -classpath ./graphipedia/graphipedia-dataimport/target/graphipedia-dataimport.jar org.graphipedia.dataimport.ExtractLinks - enwiki-links.xml
	java -Xmx20G -classpath ./graphipedia/graphipedia-dataimport/target/graphipedia-dataimport.jar org.graphipedia.dataimport.neo4j.ImportGraph enwiki-links.xml graph.db

Move the graph.db folder in to your `<neo4j dir>\data\databases` directory and start Neo4j.

Delete duplicates:

	MATCH (thing:Thing)
	WITH thing.name AS name, count(*) as cnt, COLLECT(ID(thing)) as ids
	WHERE cnt > 1
	WITH name, cnt, last(ids) AS nodeIds
	WITH COLLECT(nodeIds) AS collection
	MATCH (n) WHERE id(n) IN collection
	DETACH DELETE n


We need the Import Max Mind stored procedure from https://github.com/maxdemarzi/import_maxmind_sproc follow these instructions.

	mvn clean package
	cp target/importer-1.0-SNAPSHOT.jar to <neo4j dir>/plugins/.

	CALL com.maxdemarzi.schema.generate;
	CALL com.maxdemarzi.import.locations("/home/maxdemarzi/GeoLite2-City-CSV_20180905/GeoLite2-City-Locations-en.csv");
	CALL com.maxdemarzi.import.ip4("/home/maxdemarzi/GeoLite2-City-CSV_20180905/GeoLite2-City-Blocks-IPv4.csv");

Delete the Metro Areas:

	MATCH (n:Metro) DETACH DELETE n

Delete `importer-1.0-SNAPSHOT.jar` from `<neo4j dir>/plugins/.`.

We also need the extension, follow the readme in the extension directory.

Replace this line in your conf/application.conf:

	bunny.key="your bunny key"

Build it:

	mvn clean package

Run it:

	    java -jar ./target/website-1.0-SNAPSHOT.jar prod
