package com.maxdemarzi;

import com.maxdemarzi.models.Post;
import com.maxdemarzi.models.Tag;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.maxdemarzi.models.User;

import java.util.List;

public interface API {

    @GET("users/{username}")
    Call<User> getUser(@Path("username") String username);

    @GET("users/{username}/profile")
    Call<User> getProfile(@Path("username") String username, @Query("username2") String username2);

    @POST("users")
    Call<User> createUser(@Body User user);

    @GET("users/{username}/posts")
    Call<List<Post>> getPosts(@Path("username") String username);

    @POST("users/{username}/posts")
    Call<Post> createPost(@Body Post post,
                          @Path("username") String username);

    @PUT("users/{username}/posts/{time}")
    Call<Post> updatePost(@Path("username") String username,
                          @Path("time") Long time);

    @GET("users/{username}/likes")
    Call<List<Post>> getLikes(@Path("username") String username);

    @POST("users/{username}/likes/{username2}/{time}")
    Call<Post> createLikes(@Path("username") String username,
                           @Path("username2") String username2,
                           @Path("time") Long time);

    @DELETE("users/{username}/likes/{username2}/{time}")
    Call<Post> removeLikes(@Path("username") String username,
                           @Path("username2") String username2,
                           @Path("time") Long time);

    @GET("users/{username}/blocks")
    Call<List<Post>> getBlocks(@Path("username") String username);

    @POST("users/{username}/blocks/{username2}/")
    Call<Post> createBlocks(@Path("username") String username,
                            @Path("username2") String username2);

    @DELETE("users/{username}/blocks/{username2}")
    Call<Post> removeBlocks(@Path("username") String username,
                            @Path("username2") String username2);

    @GET("users/{username}/mentions")
    Call<List<Post>> getMentions(@Path("username") String username);

    @GET("users/{username}/timeline")
    Call<List<Post>> getTimeline(@Path("username") String username);

    @GET("tags")
    Call<List<Tag>> getTags();

    @GET("tags/{tag}")
    Call<List<Post>> getTag(@Path("tag") String tag,
                            @Query("username") String username);

    @GET("search")
    Call<List<Post>> getSearch(@Query("q") String q,
                               @Query("username") String username);

    @GET("search/latest")
    Call<List<Post>> getLatest(@Query("username") String username);
}
