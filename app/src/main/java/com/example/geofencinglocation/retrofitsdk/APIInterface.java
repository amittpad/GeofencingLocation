package com.example.geofencinglocation.retrofitsdk;


import com.example.geofencinglocation.retrofitsdk.response.LocationListResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIInterface {
    @GET("geofence")
    Call<LocationListResponse> getGeoFencingResponse();

//    @FormUrlEncoded
//    @POST("registration")
//    Call<RegistrationResponse> getRegistrationResponse(
//            @Field("first_name") String firstName,
//            @Field("last_name") String lastName,
//            @Field("email") String email,
//            @Field("password") String password,
//            @Field("phone") String phone,
//            @Field("state") String state,
//            @Field("city") String city,
//            @Field("address1") String address1);

}

