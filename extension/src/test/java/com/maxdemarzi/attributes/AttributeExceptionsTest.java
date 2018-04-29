package com.maxdemarzi.attributes;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class AttributeExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(AttributeExceptions.invalidInput);
        exceptions.add(AttributeExceptions.emptyNameParameter);
        exceptions.add(AttributeExceptions.missingNameParameter);
        exceptions.add(AttributeExceptions.attributeNotFound);

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
