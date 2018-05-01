package com.maxdemarzi.cities;

import com.maxdemarzi.Exceptions;

class CityExceptions extends Exceptions {

    static final Exceptions cityNotFound = new Exceptions(400, "City not Found.");

    CityExceptions(int code, String error) {
       super(code, error);
   }
}