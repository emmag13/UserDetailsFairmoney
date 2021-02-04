package com.demoproject.restapi;

import com.demoproject.restapi.models.Data;
import com.demoproject.restapi.models.UserDetailsData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface UserDetailsAPI {
    @GET("user/{userId}")
    Call<UserDetailsData> getUserDetailData(@Header("app-id") String appID, @Path("userId") String userID);

    @GET("user?limit=100")
    Call<Data> getUsersDetails(@Header("app-id") String appID);
}
