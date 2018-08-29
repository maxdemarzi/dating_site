package com.maxdemarzi.routes;

import com.maxdemarzi.API;
import com.maxdemarzi.App;
import com.maxdemarzi.BunnyCDN;
import com.maxdemarzi.models.Post;
import okhttp3.*;
import okhttp3.MediaType;
import org.jooby.*;
import org.pac4j.core.profile.CommonProfile;
import retrofit2.Response;

import java.io.File;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Posts extends Jooby {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS");

    {
        post("/post", req -> {
            API api = require(API.class);
            CommonProfile profile = require(CommonProfile.class);
            String username = profile.getUsername();

            Upload upload = req.file("file");
            String status = req.param("status").value();
            File file = upload.file();
            RequestBody body = RequestBody.create(MediaType.parse("application/octet"),
                    Files.readAllBytes(file.toPath()));

            String time = dateFormat.format(ZonedDateTime.now()) + getFileExtension(upload.name());

            BunnyCDN bunny = require(BunnyCDN.class);
            Response<ResponseBody> bunnyResponse = bunny.upload("fives", username, time, body).execute();
            if (bunnyResponse.isSuccessful()) {
                Post post = new Post();
                post.setStatus(status);
                post.setFilename(username + "/" + time);
                Response<Post> response = api.createPost(username, post).execute();
                if (response.isSuccessful()) {
                    return Results.redirect("/user/" + username);
                }
            }
            throw new Err(Status.BAD_REQUEST);
        });
    }

    private static String getFileExtension(String filename) {
        if(filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0)
            return filename.substring(filename.lastIndexOf("."));
        else return "";
    }
}
