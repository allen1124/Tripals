package com.hku.tripals.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hku.tripals.MainActivity;
import com.hku.tripals.R;
import com.hku.tripals.StartActivity;
import com.hku.tripals.model.Place;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

    private static final String TAG = "ParserTask";
    private static final String BOOKMARK_PREF = "BOOKMARK_PREF";
    JSONObject jObject;
    private HashMap<String, Boolean> markerList;
    private GoogleMap mMap;
    private Context context;
    SharedPreferences bookmarkPref;
    boolean bookmarked = false;

    public ParserTask(Context context, GoogleMap mMap, HashMap<String, Boolean> markerList) {
        this.context = context;
        this.mMap = mMap;
        this.markerList = markerList;
    }

    @Override
    protected List<HashMap<String, String>> doInBackground(String... jsonData) {

        List<HashMap<String, String>> places = null;
        Place_JSON placeJson = new Place_JSON();

        try {
            jObject = new JSONObject(jsonData[0]);
            places = placeJson.parse(jObject);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return places;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {

        if(list == null){
            Log.d(TAG, "onPostExecute: List is empty");
            return;
        }
        Log.d(TAG, "list size: " + list.size());
        if(list.size() == 21){
            String nextPageToken = list.get(20).get("next_page_token");
            final String nextPageUrl = nearbyUrlNextPageBuilder(nextPageToken).toString();
            Log.d(TAG, "request url: "+nextPageUrl);
            final PlacesTask placesTask = new PlacesTask(context, mMap, markerList);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    placesTask.execute(nextPageUrl);
                }
            }, 2000);
        }
        for (int i = 0; i < list.size() && i < 20; i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> hmPlace = list.get(i);
            if(markerList.get(hmPlace.get("place_id")) != null && markerList.get(hmPlace.get("place_id"))){
                break;
            }else{
                markerList.put(hmPlace.get("place_id"), true);
            }
            Place place = new Place();
            place.setLatitude(hmPlace.get("lat"));
            double lat = Double.parseDouble(hmPlace.get("lat"));
            place.setLongitude(hmPlace.get("lng"));
            double lng = Double.parseDouble(hmPlace.get("lng"));
            String name = hmPlace.get("place_name");
            place.setName(name);
            Log.d("Map", "place: " + name);
            String vicinity = hmPlace.get("vicinity");
            place.setVicinity(vicinity);
            LatLng latLng = new LatLng(lat, lng);
            place.setPlaceId(hmPlace.get("place_id"));
            place.setPhotoReference(hmPlace.get("photo_reference"));
            place.setRating(Double.parseDouble(hmPlace.get("rating")));
            markerOptions.position(latLng);
            markerOptions.title(name);
            bookmarkPref = context.getSharedPreferences(BOOKMARK_PREF, MODE_PRIVATE);
            bookmarked = bookmarkPref.getBoolean(hmPlace.get("place_id"), false);
            if(bookmarked){
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            Marker m = mMap.addMarker(markerOptions);
            m.setTag(place);
        }
    }

    public StringBuilder nearbyUrlNextPageBuilder(String pageToken){
        SharedPreferences sharedPreferences = context.getSharedPreferences("keys" , MODE_PRIVATE);
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("&pagetoken=" + pageToken);
        sb.append("&key="+sharedPreferences.getString("place_key" , ""));
        return sb;
    }
}