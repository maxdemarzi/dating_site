package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.schema.Properties.*;


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
            } else {
                input.put(USERNAME, Jsoup.clean(username, Whitelist.none()).toLowerCase());
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
            } else {
                input.put(EMAIL, Jsoup.clean(email, Whitelist.none()));
            }
        }

        if (!input.containsKey(NAME)) {
            throw UserExceptions.missingNameParameter;
        } else {
            String name = (String) input.get(NAME);
            if (name.equals("")) {
                throw UserExceptions.emptyNameParameter;
            } else {
                input.put(NAME, Jsoup.clean(name, Whitelist.none()));
            }
        }

        if (!input.containsKey(PASSWORD)) {
            throw UserExceptions.missingPasswordParameter;
        } else {
            String password = (String) input.get(PASSWORD);
            if (password.equals("")) {
                throw UserExceptions.emptyPasswordParameter;
            }
        }

        if (!input.containsKey(BIO)) {
            throw UserExceptions.missingBioParameter;
        } else {
            String bio = (String) input.get(BIO);
            if (bio.equals("")) {
                throw UserExceptions.emptyBioParameter;
            } else{
                input.put(BIO, Jsoup.clean(bio, Whitelist.none()));
            }
        }

        if (!input.containsKey(IS)) {
            throw UserExceptions.missingIsParameter;
        } else {
            String is = (String) input.get(IS);
            if (is.equals("")) {
                throw UserExceptions.emptyIsParameter;
            } else {
                input.put(IS, Jsoup.clean(is, Whitelist.none()));
            }
        }

        if (!input.containsKey(IS_LOOKING_FOR)) {
            throw UserExceptions.missingIsLookingForParameter;
        } else {
            if (input.get(IS_LOOKING_FOR) == null) {
                throw UserExceptions.emptyIsLookingForParameter;
            }
            ArrayList<String> isLookingFor = (ArrayList<String>)input.get(IS_LOOKING_FOR);

            if (isLookingFor.size() == 0) {
                throw UserExceptions.emptyIsLookingForParameter;
            } else {
                ArrayList<String> cleanIsLookingFor = new ArrayList<>();
                for (String item : isLookingFor) {
                    cleanIsLookingFor.add(Jsoup.clean(item, Whitelist.none()));
                }
                input.put(IS_LOOKING_FOR, cleanIsLookingFor.toArray(new String[]{}));
            }
        }

        if (!input.containsKey(DISTANCE)) {
            throw UserExceptions.missingDistanceParameter;
        } else {
            if (input.get(DISTANCE) instanceof Number) {
                Number distance = (Number)input.get(DISTANCE);
                if (distance == null) {
                    throw UserExceptions.emptyDistanceParameter;
                }
                input.put(DISTANCE, distance.longValue());
            } else {
                throw UserExceptions.invalidDistanceParameter;
            }
        }

        if (!input.containsKey(CITY)) {
            throw UserExceptions.missingCityParameter;
        } else {
            String city = (String) input.get(CITY);
            if (city.equals("")) {
                throw UserExceptions.emptyCityParameter;
            } else {
                input.put(CITY, Jsoup.clean(city, Whitelist.none()));
            }
        }

        return input;
    }

    public static HashMap update(String body) throws IOException {
        HashMap<String, Object> input;

        if (body == null) {
            throw Exceptions.invalidInput;
        }

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exception e) {
            throw Exceptions.invalidInput;
        }

        if (input.containsKey(EMAIL)) {
            String email = (String)input.get(EMAIL);
            if (email.equals("")) {
                input.remove(EMAIL);
            } else if (!email.contains("@")) {
                throw UserExceptions.invalidEmailParameter;
            } else {
                input.put(EMAIL, Jsoup.clean(email, Whitelist.none()));
            }
        }

        if (input.containsKey(NAME)) {
            String name = (String) input.get(NAME);
            if (name.equals("")) {
                input.remove(NAME);
            } else {
                input.put(NAME, Jsoup.clean(name, Whitelist.none()));
            }
        }

        if (input.containsKey(PASSWORD)) {
            String password = (String) input.get(PASSWORD);
            if (password.equals("")) {
                input.remove(PASSWORD);
            }
        }

        if (input.containsKey(IS)) {
            String is = (String) input.get(IS);
            if (is.equals("")) {
                input.remove(IS);
            } else {
                input.put(IS, Jsoup.clean(is, Whitelist.none()));
            }
        }

        if (input.containsKey(IS_LOOKING_FOR)) {
            ArrayList<String> isLookingFor = new ArrayList<>();
            if (input.get(IS_LOOKING_FOR) == null) {
                input.remove(IS_LOOKING_FOR);
            } else {
                isLookingFor = (ArrayList<String>) input.get(IS_LOOKING_FOR);
            }
            if (isLookingFor.size() == 0) {
                input.remove(IS_LOOKING_FOR);
            } else {
                ArrayList<String> cleanIsLookingFor = new ArrayList<>();
                for (String item : isLookingFor) {
                    cleanIsLookingFor.add(Jsoup.clean(item, Whitelist.none()));
                }
                input.put(IS_LOOKING_FOR, cleanIsLookingFor.toArray(new String[]{}));
            }
        }

        if (input.containsKey(DISTANCE)) {
            if (input.get(DISTANCE) instanceof Number) {
                Number distance = (Number)input.get(DISTANCE);
                if (distance == null) {
                    throw UserExceptions.emptyDistanceParameter;
                }
                input.put(DISTANCE, distance.longValue());
            } else {
                input.remove(DISTANCE);
            }
        }

        if (input.containsKey(CITY)) {
            String city = (String) input.get(CITY);
            if (city.equals("")) {
                input.remove(CITY);
            } else {
                input.put(CITY, Jsoup.clean(city, Whitelist.none()));
            }
        }

        return input;
    }
}
