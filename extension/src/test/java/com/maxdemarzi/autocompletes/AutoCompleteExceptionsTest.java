package com.maxdemarzi.autocompletes;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import com.maxdemarzi.autocomplete.AutoCompleteExceptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class AutoCompleteExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(AutoCompleteExceptions.invalidInput);
        exceptions.add(AutoCompleteExceptions.labelNotValid);
        exceptions.add(AutoCompleteExceptions.propertyNotValid);

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
