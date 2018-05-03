package com.maxdemarzi.countries;

import com.maxdemarzi.Exceptions;

class CountryExceptions extends Exceptions {

    static final Exceptions countryNotFound = new Exceptions(400, "Country not Found.");

    CountryExceptions(int code, String error) {
       super(code, error);
   }
}