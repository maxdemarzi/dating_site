package com.maxdemarzi;

import com.maxdemarzi.models.Post;
import com.maxdemarzi.models.Tag;
import com.maxdemarzi.models.User;
import com.typesafe.config.Config;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.pac4j.Pac4j;
import org.jooby.rocker.Rockerby;
import org.jooby.whoops.Whoops;
import org.mindrot.jbcrypt.BCrypt;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import views.index;
import views.login;
import views.register;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static java.lang.Thread.sleep;


public class App extends Jooby {
    static API api;
  {

      // Debug friendly error messages
      on("dev", () -> use(new Whoops()));

      // Configure Jackson
      use(new Jackson().doWith(mapper -> {
          mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
      }));

      // Setup Template Engine
      use(new Rockerby());

      // Setup API
      onStart(registry -> {

          Config conf = require(Config.class);

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

          api = retrofit.create(API.class);
      });

      // Configure public static files
      assets("/assets/**");
      assets("/favicon.ico", "/assets/favicon.ico");

      get("/", index::template);
      get("/login", login::template);
      get("/register", register::template);
      post("/register", (req, rsp) -> {
          User user = req.form(User.class);
          user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
          Response<User> response = api.createUser(user).execute();
          if (response.isSuccessful()) {
              Results.redirect("/login");
          } else {
              throw new Err(Status.CONFLICT, "There was a problem with your registration.");
          }
      });

      use("*", (req, rsp, chain) -> {
          ProfileManager pm = require(ProfileManager.class);
          CommonProfile profile = (CommonProfile) pm.get(req.ifSession().isPresent()).orElseGet( () -> getAnonymous());

          req.set("requested_by", profile.getUsername());
          chain.next(req, rsp);
      });

      get("/user/{username}/likes", req -> {
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = getUserProfile(requested_by);

          Response<User> userResponse = api.getProfile(req.param("username").value(), requested_by).execute();
          if (userResponse.isSuccessful()) {
              User user = userResponse.body();

              Response<List<Post>> timelineResponse = api.getLikes(req.param("username").value()).execute();
              List<Post> posts = new ArrayList<>();
              if (timelineResponse.isSuccessful()) {
                  posts = timelineResponse.body();
              }
              return views.home.template(authenticated, user, posts, getTags());
          } else {
              throw new Err(Status.BAD_REQUEST);
          }
      });

      get("/user/{username}", req -> {
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = getUserProfile(requested_by);

          Response<User> userResponse = api.getProfile(req.param("username").value(), requested_by).execute();
          if (userResponse.isSuccessful()) {
              User user = userResponse.body();

              Response<List<Post>> timelineResponse = api.getPosts(req.param("username").value()).execute();
              List<Post> posts = new ArrayList<>();
              if (timelineResponse.isSuccessful()) {
                  posts = timelineResponse.body();
              }

              return views.user.template(authenticated, user, posts, getTags());
          } else {
              throw new Err(Status.BAD_REQUEST);
          }
      });

      get("/tag/{hashtag}", req -> {
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = getUserProfile(requested_by);

          Response<List<Post>> tagResponse = api.getTag(req.param("hashtag").value(), requested_by).execute();
          List<Post> posts = new ArrayList<>();
          if (tagResponse.isSuccessful()) {
              posts = tagResponse.body();
          }
          return views.home.template(authenticated, authenticated, posts, getTags());
      });

      post("/search", req -> {
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = getUserProfile(requested_by);

          Response<List<Post>> searchResponse = api.getSearch(req.param("q").value(), requested_by).execute();
          List<Post> posts = new ArrayList<>();
          if (searchResponse.isSuccessful()) {
              posts = searchResponse.body();
          }

          return views.home.template(authenticated, authenticated, posts, getTags());
      });

      get("/explore", req -> {
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = getUserProfile(requested_by);

          Response<List<Post>> searchResponse = api.getLatest(requested_by).execute();
          List<Post> posts = new ArrayList<>();
          if (searchResponse.isSuccessful()) {
              posts = searchResponse.body();
          }

          return views.home.template(authenticated, authenticated, posts, getTags());

      });

      use(new Pac4j().client(conf -> new FormClient("/login", new ServiceAuthenticator())));

      get("/home", req -> {
          // TODO: 4/27/18 Allow anonymous to view home?
          CommonProfile profile = require(CommonProfile.class);
          String username = profile.getUsername();
          User authenticated = getUserProfile(username);

          Response<List<Post>> timelineResponse = api.getTimeline(username).execute();
          List<Post> posts = new ArrayList<>();
          if (timelineResponse.isSuccessful()) {
              posts = timelineResponse.body();
          }

          return views.home.template(authenticated, authenticated, posts, getTags());
      });

      post("/post", req -> {
          Post post = req.form(Post.class);
          CommonProfile profile = require(CommonProfile.class);
          String username = profile.getUsername();

          Response<Post> response = api.createPost(post, username).execute();
          if (response.isSuccessful()) {
              sleep(1000);
              return Results.redirect("/home");
          } else {
              throw new Err(Status.BAD_REQUEST);
          }
      });

  }

    private User getUserProfile(String id) throws java.io.IOException {
        User user = null;
        if (id != null) {
            Response<User> userResponse = api.getProfile(id, null).execute();
            if (userResponse.isSuccessful()) {
                user = userResponse.body();
            } else {
                throw new Err(Status.BAD_REQUEST);
            }
        }
        return user;
    }

    private List<Tag> getTags() throws java.io.IOException {
        List<Tag> trends = new ArrayList<>();
        Response<List<Tag>> trendsResponce = api.getTags().execute();
        if (trendsResponce.isSuccessful()) {
            trends = trendsResponce.body();
        }
        return trends;
    }

    private CommonProfile getAnonymous() {
        CommonProfile anonymous = new CommonProfile();
        anonymous.addAttribute("username", "anonymous");
        return anonymous;
    }

  public static void main(final String[] args) {
    run(App::new, args);
  }

}
