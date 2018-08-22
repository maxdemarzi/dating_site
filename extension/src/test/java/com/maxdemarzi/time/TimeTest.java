package com.maxdemarzi.time;

import com.maxdemarzi.Time;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

public class TimeTest {

    @Test
    public void shouldGetLatestTime() {
        Time time = new Time();
        Assert.assertNotNull(time);

        ZonedDateTime actual = Time.getLatestTime(null);
        Assert.assertTrue(actual.plusSeconds(1).isAfter(ZonedDateTime.now(Time.utc)));

        actual = Time.getLatestTime("2018-05-01T13:00:01Z");
        Assert.assertTrue(actual.plusSeconds(1).isBefore(ZonedDateTime.now(Time.utc)));
    }

}
