package com.maxdemarzi.has;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class HasExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(HasExceptions.invalidInput);
        exceptions.add(HasExceptions.alreadyHasAttribute);
        exceptions.add(HasExceptions.notHavingAttribute);
        exceptions.add(new HasExceptions(400, "new exception"));

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
