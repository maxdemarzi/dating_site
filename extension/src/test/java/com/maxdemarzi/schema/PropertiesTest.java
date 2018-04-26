package com.maxdemarzi.schema;

import com.maxdemarzi.schema.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.maxdemarzi.schema.Properties.EMAIL;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.PASSWORD;
import static com.maxdemarzi.schema.Properties.STATUS;
import static com.maxdemarzi.schema.Properties.TIME;
import static com.maxdemarzi.schema.Properties.USERNAME;
import static org.junit.Assert.assertEquals;

public class PropertiesTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void shouldNotLetYouCallConstructor() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        thrown.expect(InvocationTargetException.class);
        Constructor<Properties> constructor;
        constructor = Properties.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();

    }

    @Test
    public void shouldTestProperties() {
        assertEquals("email", EMAIL);
        assertEquals("name", NAME);
        assertEquals("password", PASSWORD);
        assertEquals("username", USERNAME);
        assertEquals("status", STATUS);
        assertEquals("time", TIME);
    }

}
