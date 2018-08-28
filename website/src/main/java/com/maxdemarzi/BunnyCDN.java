package com.maxdemarzi;

import okhttp3.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface BunnyCDN {

    @PUT("{storageZoneName}/{path}/{fileName}")
    Call<ResponseBody> upload(@Path("storageZoneName") String storageZoneName,
                              @Path("path") String path,
                              @Path("fileName") String fileName,
                              @Body RequestBody body);

}
