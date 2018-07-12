package com.maxdemarzi;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import java.time.ZonedDateTime;

public class CustomObjectMapper extends ObjectMapper {

    private CustomObjectMapper(){}

    private static class CustomObjectMapperHelper {
        private static final SimpleModule module = new SimpleModule("ZonedDateTime", new Version( 1, 0, 0, "" ));
        private static final CustomObjectMapper INSTANCE = new CustomObjectMapper();

        static {
            module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
            INSTANCE.registerModule(module);
        }
    }

    public static CustomObjectMapper getInstance() {
        return CustomObjectMapperHelper.INSTANCE;
    }
}
