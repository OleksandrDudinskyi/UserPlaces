package com.dudinskyi.userplaces;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Near by search service
 *
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public interface NearBySearchService {
    @GET("/nearbysearch/json")
    void nearBySearch(@QueryMap Map<String, String> options, Callback<NearBySearchResult> callback);
}
