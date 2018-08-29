package com.maxdemarzi;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jooby.Env;
import org.jooby.Jooby;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class Neo4jApi implements Jooby.Module {

  @Override public void configure(Env env, Config conf, Binder binder) throws Throwable {
    // Define the interceptor, add authentication headers
    String credentials = Credentials.basic(conf.getString("neo4j.username"), conf.getString("neo4j.password"));
    Interceptor interceptor = chain -> {
      Request newRequest = chain.request().newBuilder().addHeader("Authorization", credentials).build();
      return chain.proceed(newRequest);
    };

    // Add the interceptor to OkHttpClient
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.interceptors().add(interceptor);
    OkHttpClient client = builder.build();

    Retrofit retrofit = new Retrofit.Builder()
        .client(client)
        .baseUrl("http://" + conf.getString("neo4j.url") + conf.getString("neo4j.prefix") +  "/")
        .addConverterFactory(JacksonConverterFactory.create())
        .build();

    API api = retrofit.create(API.class);
    binder.bind(API.class).toInstance(api);
  }
}
