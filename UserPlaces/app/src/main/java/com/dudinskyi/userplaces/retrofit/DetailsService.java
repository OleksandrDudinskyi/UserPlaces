package com.dudinskyi.userplaces.retrofit;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Near by search service
 *
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public interface DetailsService {
    @GET("/details/json")
    public void nearBySearch(@QueryMap Map<String, String> options, Callback<DetailsResult> callback);
}
