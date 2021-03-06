package com.dudinskyi.userplaces;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.dudinskyi.userplaces.retrofit.NearBySearchService;
import com.dudinskyi.userplaces.retrofit.SearchResult;
import com.dudinskyi.userplaces.retrofit.TextSearchService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Start activity
 *
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public class MainActivity extends ActionBarActivity implements
        SearchView.OnQueryTextListener, View.OnClickListener {
    private enum SearchType {
        NEARBY, TEXT;
    }

    public static final String YOUR_API_KEY = "YOUR_API_KEY";
    private static final String QUERY_PARAM_KEY = "QUERY_PARAM_KEY";
    private SearchView mSearchView;
    private TextView mWelcomeTextView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mSearchAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RestAdapter mRestAdapter;
    private LocationManager mLocationManager;
    private String mQuery;
    private double mUserLocationLat = 0;
    private double mUserLocationLng = 0;
    private static final int GET_SEARCH_TYPE = 0;
    private SearchType mSearchType = SearchType.NEARBY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mWelcomeTextView = (TextView) findViewById(R.id.welcome_text_view);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastLocation != null) {
            mUserLocationLat = lastLocation.getLatitude();
            mUserLocationLng = lastLocation.getLongitude();
        }
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        int numberOfColumns = getResources().getInteger(R.integer.number_of_columns);
        mLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mSearchAdapter);
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/place")
                .build();
        if (savedInstanceState != null && savedInstanceState.containsKey(QUERY_PARAM_KEY)) {
            searchQueryRequest(savedInstanceState.getString(QUERY_PARAM_KEY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(settingsIntent, GET_SEARCH_TYPE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_SEARCH_TYPE && resultCode == RESULT_OK) {
            if (SettingsActivity.NEARBY_SEARCH.equals(data.getExtras().getString(SettingsActivity.SEARCH_TYPE_KEY))) {
                mSearchType = SearchType.NEARBY;
            } else {
                mSearchType = SearchType.TEXT;
            }
        }
    }

    private void setupSearchView(MenuItem searchItem) {
        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            mSearchView.setSearchableInfo(info);
        }
        mSearchView.setOnQueryTextListener(this);
    }

    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(QUERY_PARAM_KEY, mQuery);
        super.onSaveInstanceState(outState);
    }

    public boolean onQueryTextSubmit(String query) {
        searchQueryRequest(query);
        return false;
    }

    private void searchQueryRequest(String query) {
        mQuery = query;
        Map<String, String> searchParams = new HashMap<>();
        Callback<SearchResult> callback = new Callback<SearchResult>() {
            @Override
            public void success(SearchResult nearBySearchResults, Response response) {
                if (response.getStatus() == 200) {
                    mSearchAdapter = new SearchAdapter(nearBySearchResults);
                    mWelcomeTextView.setVisibility(View.GONE);
                    mRecyclerView.swapAdapter(mSearchAdapter, true);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
            }
        };
        searchParams.put("query", query);
        searchParams.put("key", YOUR_API_KEY);
        if (SearchType.NEARBY == mSearchType) {
            searchParams.put("location", mUserLocationLat + "," + mUserLocationLng);
            searchParams.put("rankBy", "distance");
            searchParams.put("sensor", "true");
            searchParams.put("radius", "500");
            NearBySearchService service = mRestAdapter.create(NearBySearchService.class);
            service.search(searchParams, callback);
        } else {
            TextSearchService service = mRestAdapter.create(TextSearchService.class);
            service.search(searchParams, callback);
        }
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.PLACE_ID_KEY, (String) v.getTag());
        startActivity(intent);
    }

    class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private SearchResult mNearBySearchResult;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View rootView;
            public TextView placeTitle;
            public ImageView imageView;
            public TextView distance;

            public ViewHolder(View v) {
                super(v);
                rootView = v;
                v.setOnClickListener(MainActivity.this);
                placeTitle = (TextView) v.findViewById(R.id.search_item_name);
                imageView = (ImageView) v.findViewById(R.id.icon);
                distance = (TextView) v.findViewById(R.id.distance);
            }
        }

        public SearchAdapter(SearchResult nearBySearchJsonResult) {
            mNearBySearchResult = nearBySearchJsonResult;
        }

        @Override
        public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item_layout, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.rootView.setTag(mNearBySearchResult.results.get(position).place_id);
            holder.placeTitle.setText(mNearBySearchResult.results.get(position).name);
            ImageLoader.getInstance().displayImage(mNearBySearchResult.results.get(position).icon,
                    holder.imageView);
            Location userLocation = new Location("UserLocation");
            userLocation.setLatitude(mUserLocationLat);
            userLocation.setLongitude(mUserLocationLng);
            Location placeLocation = new Location("Place Location");
            placeLocation.setLatitude(mNearBySearchResult.results.get(position).geometry.location.lat);
            placeLocation.setLongitude(mNearBySearchResult.results.get(position).geometry.location.lng);
            DecimalFormat df = new DecimalFormat("#");
            holder.distance.setText(df.format(userLocation.distanceTo(placeLocation)));

        }

        @Override
        public int getItemCount() {
            return mNearBySearchResult.results.size();
        }
    }
}
