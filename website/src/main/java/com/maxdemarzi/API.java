package com.maxdemarzi;

import com.maxdemarzi.models.*;
import org.jooby.Err;
import org.jooby.Status;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface API {

    @GET("attributes")
    Call<List<Attribute>> getAttributes(@Query("offset") Integer offset,
                                        @Query("limit") Integer limit,
                                        @Query("username") String username);

    @GET("attributes/{name}")
    Call<Attribute> getAttribute(@Path("name") String name);

    @GET("users/{username}")
    Call<User> getUser(@Path("username") String username);

    @GET("users/{username}/profile")
    Call<User> getProfile(@Path("username") String username,
                          @Query("username2") String username2);

    default User getUserProfile(String id) throws java.io.IOException {
        User user = null;
        if (id != null) {
            Response<User> userResponse = getProfile(id, null).execute();
            if (userResponse.isSuccessful()) {
                user = userResponse.body();
            } else {
              throw new Err(Status.BAD_REQUEST);
            }
        }
        return user;
    }

    @POST("users")
    Call<User> createUser(@Body User user);

    @GET("users/{username}/posts")
    Call<List<Post>> getPosts(@Path("username") String username);

    @POST("users/{username}/posts")
    Call<Post> createPost(@Path("username") String username,
                          @Body Post post);

    @PUT("users/{username}/posts/{time}")
    Call<Post> updatePost(@Path("username") String username,
                          @Path("time") Long time);

    @GET("users/{username}/has")
    Call<List<Attribute>> getHas(@Path("username") String username,
                                 @Query("limit") Integer limit,
                                 @Query("offset") Integer offset,
                                 @Query("username2") String username2);

    @POST("users/{username}/has/{attribute}")
    Call<Attribute> createHas(@Path("username") String username,
                                    @Path("attribute") String attribute);

    @DELETE("users/{username}/has/{attribute}")
    Call<Attribute> deleteHas(@Path("username") String username,
                                    @Path("attribute") String attribute);

    @GET("users/{username}/wants")
    Call<List<Attribute>> getWants(@Path("username") String username,
                                   @Query("limit") Integer limit,
                                   @Query("offset") Integer offset,
                                   @Query("username2") String username2);

    @POST("users/{username}/wants/{attribute}")
    Call<Attribute> createWants(@Path("username") String username,
                                    @Path("attribute") String attribute);

    @DELETE("users/{username}/wants/{attribute}")
    Call<Attribute> deleteWants(@Path("username") String username,
                                    @Path("attribute") String attribute);

    @GET("users/{username}/likes")
    Call<List<Thing>> getLikes(@Path("username") String username,
                               @Query("limit") Integer limit,
                               @Query("offset") Integer offset,
                               @Query("username2") String username2);

    @POST("users/{username}/likes/{thing}/")
    Call<Thing> createLikes(@Path("username") String username,
                            @Path("thing") String thing);

    @DELETE("users/{username}/likes/{thing}/")
    Call<Thing> deleteLikes(@Path("username") String username,
                           @Path("thing") String thing);

    @GET("users/{username}/hates")
    Call<List<Thing>> getHates(@Path("username") String username,
                               @Query("limit") Integer limit,
                               @Query("offset") Integer offset,
                               @Query("username2") String username2);

    @POST("users/{username}/hates/{thing}/")
    Call<Thing> createHates(@Path("username") String username,
                            @Path("thing") String thing);

    @DELETE("users/{username}/hates/{thing}/")
    Call<Thing> deleteHates(@Path("username") String username,
                            @Path("thing") String thing);

    @GET("users/{username}/high_fives")
    Call<List<Post>> getHighFives(@Path("username") String username,
                                  @Query("limit") Integer limit);

    @GET("users/{username}/low_fives")
    Call<List<Post>> getLowFives(@Path("username") String username);

    @POST("users/{username}/high_fives/{username2}/{id}")
    Call<Post> createHighFive(@Path("username") String username,
                           @Path("username2") String username2,
                           @Path("id") Long id);

    @DELETE("users/{username}/high_fives/{username2}/{id}")
    Call<Post> removeHighFive(@Path("username") String username,
                           @Path("username2") String username2,
                           @Path("id") Long id);

    @POST("users/{username}/low_fives/{username2}/{id}")
    Call<Post> createLowFive(@Path("username") String username,
                              @Path("username2") String username2,
                              @Path("id") Long id);

    @DELETE("users/{username}/low_fives/{username2}/{id}")
    Call<Post> removeLowFive(@Path("username") String username,
                              @Path("username2") String username2,
                              @Path("id") Long id);

    @GET("users/{username}/conversations")
    Call<List<Conversation>> getConversations(@Path("username") String username,
                                              @Query("limit") int limit);

    @GET("users/{username}/conversations/{username2}")
    Call<List<Message>> getConversation(@Path("username") String username,
                                       @Path("username2") String username2);

    @POST("users/{username}/conversations/{username2}")
    Call<Message> createMessage(@Path("username") String username,
                                @Path("username2") String username2,
                                @Body Message message);

    @GET("users/{username}/blocks")
    Call<List<User>> getBlocks(@Path("username") String username);

    @POST("users/{username}/blocks/{username2}/")
    Call<User> createBlocks(@Path("username") String username,
                            @Path("username2") String username2);

    @DELETE("users/{username}/blocks/{username2}")
    Call<User> removeBlocks(@Path("username") String username,
                            @Path("username2") String username2);

    @GET("users/{username}/mentions")
    Call<List<Post>> getMentions(@Path("username") String username);

    @GET("users/{username}/timeline")
    Call<List<Post>> getTimeline(@Path("username") String username,
                                 @Query("competition") Boolean competition);

    @GET("users/{username}/recommended")
    Call<List<User>> getRecommended(@Path("username") String username);

    @GET("tags")
    Call<List<Tag>> getTags();

    default List<Tag> getTagList() throws IOException {
      List<Tag> trends = new ArrayList<>();
      Response<List<Tag>> trendsResponce = getTags().execute();
      if (trendsResponce.isSuccessful()) {
        trends = trendsResponce.body();
      }
      return trends;
    }

    @GET("tags/{tag}")
    Call<List<Post>> getTag(@Path("tag") String tag,
                            @Query("username") String username);

    @GET("things/{thing}")
    Call<Thing> getThing(@Path("thing") String thing,
                            @Query("username") String username);


    @GET("autocompletes/City/lowercase_full_name/{query}")
    Call<List<City>> autoCompleteCity(@Path("query") String query,
                                @Query("display_property") String display_property);

    @GET("autocompletes/Attribute/lowercase_name/{query}")
    Call<List<Attribute>> autoCompleteAttribute(@Path("query") String query,
                                      @Query("display_property") String display_property);

    @GET("autocompletes/Thing/lowercase_name/{query}")
    Call<List<Thing>> autoCompleteThing(@Path("query") String query,
                                                @Query("display_property") String display_property);

    @GET("autocompletes/{label}/{property}/{query}")
    Call<Thing> getAutoCompletes(@Path("label") String label,
                                 @Path("property") String property,
                                 @Path("query") String query,
                                 @Query("display_property") String display_property);

    @GET("cities/{geoname_id}")
    Call<City> getCity(@Path("thing") String geoname_id);

    @GET("cities/{city}/{state}")
    Call<City> getCityWithState(@Path("city") String city,
                                @Path("state") String state);

    @GET("state/{code}/cities")
    Call<City> getCitiesFromState(@Path("code") String code);

    @GET("countries/{code}/states")
    Call<City> getStatesFromCountry(@Path("code") String code);

    @GET("countries")
    Call<City> getCountries();

    @GET("search")
    Call<List<Post>> getSearch(@Query("q") String q,
                               @Query("username") String username);

    @GET("latest")
    Call<List<Post>> getLatest(@Query("geoid") String geoid, @Query("username") String username);
}
