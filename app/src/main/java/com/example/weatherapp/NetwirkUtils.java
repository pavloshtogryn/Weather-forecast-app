package com.example.bottomnavigationtest;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

public class NetwirkUtils {
    private static final String HEROKU_API_BASE_URL_SIN="https://xxxxxxxxxxx.herokuapp.com";
    private static final String HEROKU_CUR_WEATH_DATA_SIN="/sinoptik";
    private static final String PARAM_CITY_NAME="q";

    private static final String OPENWEATHER_API_BASE_URL="http://api.openweathermap.org";
    private static final String OPENWEATHER_CUR_WEATH_DATA="/data/2.5/weather";
    private static final String PARAM_UNITS="units";
    private static final String PARAM_KEY="APPID";
    public static URL generateURL_SINOPTIK (String city_name){
        Uri builtUri=Uri.parse(HEROKU_API_BASE_URL_SIN+HEROKU_CUR_WEATH_DATA_SIN)
                .buildUpon()
                .appendQueryParameter(PARAM_CITY_NAME, city_name)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch(MalformedURLException e){
            e.printStackTrace();

        }
        return url;
    }

    public static URL generateURL_OWM (String city_name){
        Uri builtUri=Uri.parse(OPENWEATHER_API_BASE_URL+OPENWEATHER_CUR_WEATH_DATA)
                .buildUpon()
                .appendQueryParameter(PARAM_CITY_NAME, city_name)
                .appendQueryParameter(PARAM_UNITS, "metric")
                .appendQueryParameter(PARAM_KEY, "")
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch(MalformedURLException e){
            e.printStackTrace();

        }
        return url;
    }

    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            int code = urlConnection.getResponseCode();
            //return Integer.toString(code);
            if (code != 404 && code!=302) {
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            }else{
                return "404";
            }

        } catch (UnknownHostException e){
            return null;
        }finally {
            urlConnection.disconnect();
        }
    }

    public static String getResponseFromUrl_1(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            int code = urlConnection.getResponseCode();
            //return Integer.toString(code);
            if (code != 404 && code!=302) {
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            }else{
                return "404";
            }

        } catch (UnknownHostException e){
            return null;
        }finally {
            urlConnection.disconnect();
        }
    }


}
