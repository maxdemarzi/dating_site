package com.maxdemarzi;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import okhttp3.OkHttpClient;
import org.jooby.Env;
import org.jooby.Jooby;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class BunnyApi implements Jooby.Module {
  @Override public void configure(Env env, Config conf, Binder binder) throws Throwable {
    // Add AccessKey header
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.addInterceptor(chain -> chain.proceed(
        chain.request().newBuilder()
            .addHeader("AccessKey", conf.getString("bunny.key"))
            .addHeader("Content-Type", "application/octet-stream")
            .build()));
    OkHttpClient client = builder.build();

    Retrofit retrofit = new Retrofit.Builder()
        .client(client)
        .baseUrl("https://storage.bunnycdn.com/")
        .addConverterFactory(JacksonConverterFactory.create())
        .build();

    BunnyCDN bunny = retrofit.create(BunnyCDN.class);
    binder.bind(BunnyCDN.class).toInstance(bunny);
  }
}
