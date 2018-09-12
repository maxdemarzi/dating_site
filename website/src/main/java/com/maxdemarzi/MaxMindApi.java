package com.maxdemarzi;

import com.google.inject.Binder;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;

public class MaxMindApi implements Jooby.Module {
    static DatabaseReader reader;

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {

        ClassLoader classLoader = getClass().getClassLoader();
        File database = new File(classLoader.getResource("com/maxdemarzi/GeoLite2-City.mmdb").getFile());

        // A File object pointing to your GeoIP2 or GeoLite2 database
        //File database = new File("/com/maxdemarzi/GeoIP2-City.mmdb");

        // This creates the DatabaseReader object. To improve performance, reuse
        // the object across lookups. The object is thread-safe.
        reader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
    }

    static HashMap<String, String> getId(String ipaddress) {
        HashMap<String, String> result = new HashMap<>();
        try {
            InetAddress ipAddress = InetAddress.getByName(ipaddress);
            CityResponse response = reader.city(ipAddress);
            result = new HashMap<>();
            result.put("id", response.getCity().getGeoNameId().toString());
            result.put("name", response.getCity().getName());

            return result;
        } catch (Exception e){
            // Default to Chicago on failure
            result.put("id", "4887398");
            result.put("name", "Chicago");
            return result;
        }

    }
}
