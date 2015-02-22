package com.dudinskyi.userplaces;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
        SearchView.OnQueryTextListener {
    private static final String YOUR_API_KEY = "YOUR_API_KEY";
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mSearchAdapter;
    private LinearLayoutManager mLayoutManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mSearchAdapter);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com/maps/api/place")
                .build();
        NearBySearchService service = restAdapter.create(NearBySearchService.class);
        Map<String, String> searchParams = new HashMap<>();
//        nearBySearch.put("location",mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        searchParams.put("location", "-33.8670522,151.1957362");
        searchParams.put("rankBy", "distance");
        searchParams.put("sensor", "true");
        searchParams.put("query", "restaurants in Sydney");
        searchParams.put("radius", "500");
        searchParams.put("key", YOUR_API_KEY);
        service.nearBySearch(searchParams, new Callback<NearBySearchResult>() {
            @Override
            public void success(NearBySearchResult nearBySearchResults, Response response) {
                Log.d("test", "response: " + response.getStatus());
                mSearchAdapter = new SearchAdapter(nearBySearchResults);
                mRecyclerView.swapAdapter(mSearchAdapter, true);
                Log.d("test", "mSearchAdapter: " + mSearchAdapter.getItemCount());
            }

            @Override
            public void failure(RetrofitError retrofitError) {
            }
        });
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
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

    static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private NearBySearchResult mNearBySearchResult;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.search_item_name);
                mImageView = (ImageView) v.findViewById(R.id.icon);
            }
        }

        public SearchAdapter(NearBySearchResult nearBySearchJsonResult) {
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
            holder.mTextView.setText(mNearBySearchResult.results.get(position).name);
            ImageLoader.getInstance().displayImage(mNearBySearchResult.results.get(position).icon,
                    holder.mImageView);

        }

        @Override
        public int getItemCount() {
            return mNearBySearchResult.results.size();
        }
    }
}
