package com.maxdemarzi.things;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class ThingsExceptionsTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(ThingExceptions.thingNotFound);
        exceptions.add(ThingExceptions.emptyNameParameter);
        exceptions.add(ThingExceptions.missingNameParameter);
        exceptions.add(ThingExceptions.invalidInput);
        exceptions.add(new ThingExceptions(400, "new exception"));

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
