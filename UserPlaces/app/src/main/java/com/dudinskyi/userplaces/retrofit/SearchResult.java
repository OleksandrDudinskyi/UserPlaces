package com.dudinskyi.userplaces.retrofit;

import java.util.List;

/**
 * NearBySearch result response class
 *
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public class SearchResult {
    public List<Result> results;

    public class Result {
        public String name;
        public String icon;
        public Geometry geometry;
        public String place_id;
    }

    public class Location {
        public double lat;
        public double lng;
    }

    public class Geometry {
        public Location location;
    }
}
