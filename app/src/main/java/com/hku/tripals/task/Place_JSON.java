package com.hku.tripals.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.hku.tripals.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Place_JSON {

    private static final String TAG = "Place_JSON";

    public List<HashMap<String, String>> parse(JSONObject jObject) {

        JSONArray jPlaces = null;
        String nextPageToken = null;
        try {
            if(!jObject.isNull("next_page_token")) {
                Log.d(TAG, "next_page_token: " + jObject.getString("next_page_token"));
                nextPageToken = jObject.getString("next_page_token");
            }
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<HashMap<String, String>> parsedResult = getPlaces(jPlaces);
        if(nextPageToken != null){
            HashMap<String, String> pagination = new HashMap<String, String>();
            pagination.put("next_page_token", nextPageToken);
            parsedResult.add(pagination);
        }
        return parsedResult;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> place = null;
        for (int i = 0; i < placesCount; i++) {
            try {
                place = getPlace((JSONObject) jPlaces.get(i));
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject jPlace) {
        HashMap<String, String> place = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String openNow = null;
        String latitude = "";
        String longitude = "";
        String rating = "0";
        String photoReference = "";
        String placeId = "";

        try {
            if (!jPlace.isNull("name")) {
                placeName = jPlace.getString("name");
            }
            if (!jPlace.isNull("vicinity")) {
                vicinity = jPlace.getString("vicinity");
            }
            if (!jPlace.isNull("opening_hours")) {
                openNow = jPlace.getJSONObject("opening_hours").getString("open_now");
            }
            if(!jPlace.isNull("photos")){
                photoReference = jPlace.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
            }
            if(!jPlace.isNull("rating")){
                rating = jPlace.getString("rating");
            }
            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
            placeId = jPlace.getString("place_id");
            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);
            place.put("rating", rating);
            place.put("place_id", placeId);
            place.put("open_now", openNow);
            place.put("photo_reference", photoReference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}