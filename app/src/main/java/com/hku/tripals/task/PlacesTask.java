package com.hku.tripals.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class PlacesTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "PlacesTask";
    String data = null;
    private HashMap<String, Boolean> markerList;
    private Context context;
    private GoogleMap mMap;

    public PlacesTask(Context context, GoogleMap mMap, HashMap<String, Boolean> markerList){
        this.context = context;
        this.mMap = mMap;
        this.markerList = markerList;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            data = downloadUrl(strings[0]);
            Log.d(TAG, "doInBackground: "+data);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        ParserTask parserTask = new ParserTask(context, mMap, markerList);
        parserTask.execute(result);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}