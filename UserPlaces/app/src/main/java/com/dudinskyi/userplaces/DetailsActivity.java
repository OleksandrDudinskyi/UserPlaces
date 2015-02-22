package com.dudinskyi.userplaces;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import com.dudinskyi.userplaces.retrofit.DetailsResult;
import com.dudinskyi.userplaces.retrofit.DetailsService;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Details activity
 *
 * @author Oleksandr Dudinskyi (dudinskyj@gmail.com)
 */
public class DetailsActivity extends Activity {
    public static final String PLACE_ID_KEY = "PLACE_ID_KEY";
    private RestAdapter mRestAdapter;
    private TextView mPlaceTitle;
    private TextView mPlaceAddress;
    private TextView mPlaceUserRating;
    private TextView mPlaceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);
        mPlaceTitle = (TextView) findViewById(R.id.place_title);
        mPlaceAddress = (TextView) findViewById(R.id.place_address);
        mPlaceUserRating = (TextView) findViewById(R.id.place_rating);
        mPlaceUrl = (TextView) findViewById(R.id.place_url);
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/place")
                .build();
        DetailsService service = mRestAdapter.create(DetailsService.class);
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("placeid", getIntent().getExtras().getString(PLACE_ID_KEY));
        searchParams.put("key", MainActivity.YOUR_API_KEY);
        service.nearBySearch(searchParams, new Callback<DetailsResult>() {
            @Override
            public void success(DetailsResult nearBySearchResults, Response response) {
                mPlaceTitle.setText(nearBySearchResults.result.name);
                mPlaceAddress.setText(nearBySearchResults.result.formatted_address);
                mPlaceUserRating.setText(nearBySearchResults.result.user_ratings_total);
                mPlaceUrl.setText(nearBySearchResults.result.url);
                mPlaceUrl.setPaintFlags(mPlaceUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                Linkify.addLinks(mPlaceUrl, Linkify.ALL);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
            }
        });

    }
}
