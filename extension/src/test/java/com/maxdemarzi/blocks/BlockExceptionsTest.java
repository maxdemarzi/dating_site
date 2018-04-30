package com.maxdemarzi.blocks;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class BlockExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(BlockExceptions.alreadyBlockingUser);
        exceptions.add(BlockExceptions.notBlockingUser);
        exceptions.add(BlockExceptions.invalidInput);
        exceptions.add(new BlockExceptions(400, "new exception"));

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
