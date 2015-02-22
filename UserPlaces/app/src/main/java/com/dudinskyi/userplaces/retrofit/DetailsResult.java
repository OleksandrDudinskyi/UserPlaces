package com.dudinskyi.userplaces.retrofit;

/**
 * Details result response class
 *
 * @author Oleksandr Dudinskyi(dudinskyj@gmail.com)
 */
public class DetailsResult {
    public Result result;
    public String status;

    public class Result {
        public String formatted_address;
        public String name;
        public String user_ratings_total;
        public String url;
    }

}
