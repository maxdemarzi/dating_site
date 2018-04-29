package com.maxdemarzi.hates;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class HateExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(HateExceptions.invalidInput);
        exceptions.add(HateExceptions.alreadyHatesThing);
        exceptions.add(HateExceptions.notHatingThing);

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
