package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.Single;


public interface ApiInterface {
    @GET("maps/api/directions/json")
    Single<Result> getDirection(@Query("mode") String mode,
                                @Query("transit_routing_preference") String preference,
                                @Query("origin") String origin,
                                @Query("destination") String destination,
                                @Query("key") String key);
}
