package com.example.geofencinglocation.retrofitsdk;


import com.example.geofencinglocation.retrofitsdk.response.LocationListResponse;
import com.example.geofencinglocation.retrofitsdk.response.ShortestDistanceResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface APIInterface {
    @GET("geofence")
    Call<LocationListResponse> getGeoFencingResponse();

    @FormUrlEncoded
    @POST("geofence")
    Call<ShortestDistanceResponse> getShortestDistanceResponse(
            @Field("distance") double distance,
            @Field("userId") String userId);

}

