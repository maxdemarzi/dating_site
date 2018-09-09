package com.maxdemarzi;

import com.maxdemarzi.models.*;
import com.maxdemarzi.routes.*;
import com.typesafe.config.Config;
import okhttp3.*;
import okhttp3.Request;
import org.jooby.*;
import org.jooby.json.Jackson;
import org.jooby.pac4j.Pac4j;
import org.jooby.rocker.Rockerby;
import org.jooby.whoops.Whoops;
import org.mindrot.jbcrypt.BCrypt;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import retrofit2.Response;
import views.index;
import views.register;

import java.util.*;

public class App extends Jooby {
  {

      // Debug friendly error messages
      on("dev", () -> use(new Whoops()));

      // Secure it
      securePort(8443);

      // Configure Jackson
      use(new Jackson().doWith(mapper -> {
          mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
      }));

      // Setup Template Engine
      use(new Rockerby());

      // Setup API
      use(new Neo4jApi());
      use(new BunnyApi());

      // Configure public static files
      assets("/assets/**");
      assets("/favicon.ico", "/assets/favicon.ico");

      // Footer Pages
      get("/about", views.footer.about::template);
      get("/cookies", views.footer.cookies::template);
      get("/help", views.footer.help::template);
      get("/privacy", views.footer.privacy::template);
      get("/terms", views.footer.terms::template);

      // Publicly Accessible
      get("/", index::template);
      get("/register", register::template);
      post("/register", req -> {
          API api = require(API.class);
          User user = req.form(User.class);
          if (user.getUsername().equals("anonymous")) {
              throw new Err(Status.CONFLICT, "You cannot register with that username.");
          }
          user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
          Response<User> response = api.createUser(user).execute();
          if (response.isSuccessful()) {
              return Results.redirect("/");
          } else {
              throw new Err(Status.CONFLICT, "There was a problem with your registration.");
          }
      });

      use(new AutoCompletes());

      // Accessible by public or registered users
      use("*", (req, rsp, chain) -> {
          ProfileManager pm = require(ProfileManager.class);
          CommonProfile profile = (CommonProfile) pm.get(req.ifSession().isPresent()).orElseGet(this::getAnonymous);

          req.set("requested_by", profile.getUsername());
          chain.next(req, rsp);
      });

      use(new Wants());
      use(new Has());
      use(new Likes());
      use(new Hates());
      use(new Users());

      get("/tag/{hashtag}", req -> {
          API api = require(API.class);
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = api.getUserProfile(requested_by);
          String tag = req.param("hashtag").value();

          Response<List<Post>> tagResponse = api.getTag(req.param("hashtag").value(), requested_by).execute();
          List<Post> posts = new ArrayList<>();
          if (tagResponse.isSuccessful()) {
              posts = tagResponse.body();
          }
          return views.tag.template(authenticated, tag, posts, api.getTagList());
      });

      post("/search", req -> {
        API api = require(API.class);
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = api.getUserProfile(requested_by);
          String search = req.param("q").value();

          Response<List<Post>> searchResponse = api.getSearch(search, requested_by).execute();
          List<Post> posts = new ArrayList<>();
          if (searchResponse.isSuccessful()) {
              posts = searchResponse.body();
          }

          return views.search.template(authenticated, search, posts, api.getTagList());
      });

      get("/explore", req -> {
          API api = require(API.class);
          String requested_by = req.get("requested_by");
          if (requested_by.equals("anonymous")) requested_by = null;
          User authenticated = api.getUserProfile(requested_by);

          Response<List<Post>> searchResponse = api.getLatest().execute();
          List<Post> posts = new ArrayList<>();
          if (searchResponse.isSuccessful()) {
              posts = searchResponse.body();
          }

          return views.explore.template(authenticated, posts, api.getTagList());
      });

      // Accessible only by registered users
      use(new Pac4j().client(conf -> new FormClient("/", new ServiceAuthenticator(() -> require(API.class)))));

      get("/home", req -> {
          API api = require(API.class);
          CommonProfile profile = require(CommonProfile.class);
          String username = profile.getUsername();
          User authenticated = api.getUserProfile(username);

          Response<List<Post>> timelineResponse = api.getTimeline(username, false).execute();
          List<Post> posts = new ArrayList<>();
          if (timelineResponse.isSuccessful()) {
              posts = timelineResponse.body();
          }

          Response<List<Conversation>> conversationResponse = api.getConversations(username, 5).execute();
          List<Conversation> conversations = new ArrayList<>();;
          if (conversationResponse.isSuccessful()) {
              conversations = conversationResponse.body();
          }

          Response<List<Post>> fivesResponse = api.getHighFives(username, 5).execute();
          List<Post> fives = new ArrayList<>();
          if (fivesResponse.isSuccessful()) {
              fives = fivesResponse.body();
          }
          return views.home.template(authenticated, authenticated, posts, conversations, fives, api.getTagList());
      });

      get("/competition", req -> {
          API api = require(API.class);
          CommonProfile profile = require(CommonProfile.class);
          String username = profile.getUsername();
          User authenticated = api.getUserProfile(username);

          Response<List<Post>> timelineResponse = api.getTimeline(username, true).execute();
          List<Post> posts = new ArrayList<>();
          if (timelineResponse.isSuccessful()) {
              posts = timelineResponse.body();
          }

          return views.competition.template(authenticated, authenticated, posts, api.getTagList());
      });

      use(new Posts());
      use(new Attributes());
      use(new Things());
      use(new Fives());
      use(new Blocks());
      use(new Messages());
      use(new Recommended());

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
