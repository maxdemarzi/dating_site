package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;

 class UserExceptions extends Exceptions {

     static final Exceptions missingUsernameParameter = new Exceptions(400, "Missing username Parameter.");
     static final Exceptions emptyUsernameParameter = new Exceptions(400, "Empty username Parameter.");
     static final Exceptions invalidUsernameParameter = new Exceptions(400, "Invalid username Parameter.");
     static final Exceptions existingUsernameParameter = new Exceptions(400, "Existing username Parameter.");

     static final Exceptions missingEmailParameter = new Exceptions(400, "Missing email Parameter.");
     static final Exceptions emptyEmailParameter = new Exceptions(400, "Empty email Parameter.");
     static final Exceptions invalidEmailParameter = new Exceptions(400, "Invalid email Parameter.");
     static final Exceptions existingEmailParameter = new Exceptions(400, "Existing email Parameter.");

     static final Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
     static final Exceptions emptyNameParameter = new Exceptions(400, "Empty name Parameter.");

     static final Exceptions missingPasswordParameter = new Exceptions(400, "Missing password Parameter.");
     static final Exceptions emptyPasswordParameter = new Exceptions(400, "Empty password Parameter.");

     static final Exceptions missingIsParameter = new Exceptions(400, "Missing is Parameter.");
     static final Exceptions emptyIsParameter = new Exceptions(400, "Empty is Parameter.");

     static final Exceptions missingIsLookingForParameter = new Exceptions(400, "Missing is_looking_for Parameter.");
     static final Exceptions emptyIsLookingForParameter = new Exceptions(400, "Empty is_looking_for Parameter.");

     static final Exceptions missingDistanceParameter = new Exceptions(400, "Missing distance Parameter.");
     static final Exceptions emptyDistanceParameter = new Exceptions(400, "Empty distance Parameter.");
     static final Exceptions invalidDistanceParameter = new Exceptions(400, "Invalid distance Parameter.");

     static final Exceptions missingCityParameter = new Exceptions(400, "Missing city Parameter.");
     static final Exceptions emptyCityParameter = new Exceptions(400, "Empty city Parameter.");


     static final Exceptions userNotFound = new Exceptions(400, "User not Found.");

    UserExceptions(int code, String error) {
        super(code, error);
    }
}