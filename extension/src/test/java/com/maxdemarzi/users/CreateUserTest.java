package com.maxdemarzi.users;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.EMAIL;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.PASSWORD;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;

public class CreateUserTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Users.class);

    @Test
    public void shouldCreateUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), input);
        HashMap actual  = response.content();
        Assert.assertTrue(actual.containsKey(TIME));
        actual.remove(TIME);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotCreateUserInvalid() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid Input", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserInvalidUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), invalidUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserInvalidEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), invalidEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingName() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingNameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing name Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyName() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyNameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty name Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingPassword() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingPasswordInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing password Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyPassword() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyPasswordInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty password Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingBio() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingBioInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing bio Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyBio() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyBioInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty bio Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingIs() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingIsInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing is Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyIs() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyIsInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty is Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingIsLookingFor() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingIsLookingForInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing is_looking_for Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyIsLookingFor() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyIsLookingForInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty is_looking_for Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyIsLookingFor2() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyIsLookingForInput2);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty is_looking_for Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }
    @Test
    public void shouldNotCreateUserExistingUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), existingUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Existing username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserExistingEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), existingEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Existing email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
            "CREATE (chicago:City {name:'Chicago', geoname_id:'1234', full_name:'Chicago, IL'})";

    private static final HashMap input = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
        put("bio", "some bio");
        put("is", "man");
        put("is_looking_for", new String[]{"woman"});
        put("distance", 10000);
        put("city", "Chicago, IL");
    }};

    private static final HashMap missingUsernameInput = new HashMap<String, Object>() {{
        put("not_username", "maxdemarzi");
    }};

    private static final HashMap emptyUsernameInput = new HashMap<String, Object>() {{
        put("username", "");
    }};

    private static final HashMap invalidUsernameInput = new HashMap<String, Object>() {{
        put("username", " has spaces ");
    }};


    private static final HashMap missingEmailInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("not_email", "maxdemarzi@hotmail.com");
    }};

    private static final HashMap emptyEmailInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "");
    }};

    private static final HashMap invalidEmailInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "not an email address");
    }};

    private static final HashMap missingNameInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
    }};

    private static final HashMap emptyNameInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "");
    }};

    private static final HashMap missingIsInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "asdfasf");
        put("bio", "some bio");
    }};

    private static final HashMap emptyIsInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "asdfasf");
        put("bio", "some bio");
        put("is", "");
    }};

    private static final HashMap missingIsLookingForInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "asdfasf");
        put("bio", "some bio");
        put("is", "male");
    }};

    private static final HashMap emptyIsLookingForInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "asdfasf");
        put("bio", "some bio");
        put("is", "male");
        put("is_looking_for", new String[]{});
    }};

    private static final HashMap emptyIsLookingForInput2 = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "asdfasf");
        put("bio", "some bio");
        put("is", "male");
        put("is_looking_for", null);
    }};

    private static final HashMap missingPasswordInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
    }};

    private static final HashMap emptyPasswordInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "");
    }};

    private static final HashMap missingBioInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "123");
    }};

    private static final HashMap emptyBioInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "123");
        put("bio", "");
    }};

    private static final HashMap existingUsernameInput = new HashMap<String, Object>() {{
        put("username", "jexp");
        put("email", "michael@hotmail.com");
        put("name", "Michael Hunger");
        put("password", "password");
        put("bio", "some bio");
        put("is", "man");
        put("is_looking_for", new String[]{"woman"});
        put("distance", 10000);
        put("city", "Chicago, IL");
    }};

    private static final HashMap existingEmailInput = new HashMap<String, Object>() {{
        put("username", "jexp2");
        put("email", "michael@neo4j.com");
        put("name", "Michael Hunger");
        put("password", "password");
        put("bio", "some bio");
        put("is", "man");
        put("is_looking_for", new String[]{"woman"});
        put("distance", 10000);
        put("city", "Chicago, IL");
    }};

    private static final HashMap expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
        put("bio", "some bio");
        put("hash","58750f2179edbd650b471280aa66fee5");
        put("is", "man");
        put("is_looking_for", new ArrayList<String>(){{add("woman");}});
        put("distance", 10000);
    }};
}