package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.EMAIL;
import static com.maxdemarzi.schema.Properties.IS;
import static com.maxdemarzi.schema.Properties.IS_LOOKING_FOR;
import static com.maxdemarzi.schema.Properties.NAME;
import static com.maxdemarzi.schema.Properties.PASSWORD;
import static com.maxdemarzi.schema.Properties.USERNAME;


public class UserValidator {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String usernamePattern = "^[a-z0-9_]{3,32}";

    public static HashMap validate(String body) throws IOException {
        HashMap<String, Object> input;

        if ( body == null) {
            throw Exceptions.invalidInput;
        }

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exception e) {
            throw Exceptions.invalidInput;
        }

        if (!input.containsKey(USERNAME)) {
            throw UserExceptions.missingUsernameParameter;
        } else {
            String username = (String)input.get(USERNAME);
            if (username.equals("")) {
                throw UserExceptions.emptyUsernameParameter;
            } else if (!username.matches(usernamePattern)) {
                throw UserExceptions.invalidUsernameParameter;
            }
        }

        if (!input.containsKey(EMAIL)) {
            throw UserExceptions.missingEmailParameter;
        } else {
            String email = (String)input.get(EMAIL);
            if (email.equals("")) {
                throw UserExceptions.emptyEmailParameter;
            } else if (!email.contains("@")) {
                throw UserExceptions.invalidEmailParameter;
            }
        }

        if (!input.containsKey(NAME)) {
            throw UserExceptions.missingNameParameter;
        } else {
            String email = (String) input.get(NAME);
            if (email.equals("")) {
                throw UserExceptions.emptyNameParameter;
            }
        }

        if (!input.containsKey(PASSWORD)) {
            throw UserExceptions.missingPasswordParameter;
        } else {
            String email = (String) input.get(PASSWORD);
            if (email.equals("")) {
                throw UserExceptions.emptyPasswordParameter;
            }
        }

        if (!input.containsKey(IS)) {
            throw UserExceptions.missingIsParameter;
        } else {
            String is = (String) input.get(IS);
            if (is.equals("")) {
                throw UserExceptions.emptyIsParameter;
            }
        }

        if (!input.containsKey(IS_LOOKING_FOR)) {
            throw UserExceptions.missingIsLookingForParameter;
        } else {
            ArrayList<String> isLookingFor = (ArrayList<String>) input.get(IS_LOOKING_FOR);
            if (isLookingFor.size() == 0) {
                throw UserExceptions.emptyIsLookingForParameter;
            } else {
                input.put(IS_LOOKING_FOR, isLookingFor.toArray(new String[]{}));
            }
        }

        input.put(USERNAME, ((String) input.get(USERNAME)).toLowerCase());

        return input;
    }
}
