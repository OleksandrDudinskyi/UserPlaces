package com.dudinskyi.userplaces;

import java.util.List;

/**
 * NearBySearch result response class
 *
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public class NearBySearchResult {
    public List<Result> results;

    class Result {
        String name;
        String icon;
        Geometry geometry;
    }

    class Location {
        double lat;
        double lng;
    }

    class Geometry {
        Location location;
    }
}
