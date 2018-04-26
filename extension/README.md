# Extension
A Neo4j Based Dating Site Backend


# Instructions

1. Build it:

        mvn clean package

2. Copy target/extension-1.0-SNAPSHOT.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j.conf:

        dbms.unmanaged_extension_classes=com.maxdemarzi=/v1

4. Start Neo4j server.

5. Create the Schema:

        :POST /v1/schema/create
        
6. API:
         
        :GET    /v1/users/{username}   
        :GET    /v1/users/{username}/profile   
        :POST   /v1/users {username:'', password:'', email:'', name:''}
        :GET    /v1/users/{username}/posts
        :POST   /v1/users/{username}/posts {status:''}
        :PUT    /v1/users/{username}/posts/{time} {status:''}
        :POST   /v1/users/{username}/posts/{username2}/{time}
        :GET    /v1/users/{username}/likes
        :POST   /v1/users/{username}/likes/{thing}
        :DELETE /v1/users/{username}/likes/{thing}
        :GET    /v1/users/{username}/hates
        :POST   /v1/users/{username}/hates/{thing}
        :DELETE /v1/users/{username}/hates/{thing}
        :GET    /v1/users/{username}/blocks
        :POST   /v1/users/{username}/blocks/{username2}
        :DELETE /v1/users/{username}/blocks/{username2}
        :GET    /v1/users/{username}/mentions
        :GET    /v1/users/{username}/timeline
        :GET    /v1/tags
        :GET    /v1/tags/{tag}
        :GET    /v1/things/{name}
        :GET    /v1/things/{name}/
        :GET    /v1/autocompletes/{label}/{property}/{query}
        
7. Query Parameters:
        
        limit=25 or any whole number
        since=<a number representing a date in linux epoc time>
        See https://www.epochconverter.com/
        
        