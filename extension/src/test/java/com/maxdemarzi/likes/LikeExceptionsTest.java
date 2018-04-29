package com.maxdemarzi.likes;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class LikeExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(LikeExceptions.invalidInput);
        exceptions.add(LikeExceptions.alreadyLikesThing);
        exceptions.add(LikeExceptions.notLikingThing);

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
