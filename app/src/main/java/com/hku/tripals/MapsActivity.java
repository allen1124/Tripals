package com.hku.tripals;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hku.tripals.task.PlacesTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private String lat = "", lng = "";
    private String type = "";
    private CardView placeCard;
    private ImageView placePhoto;
    private TextView placeName;
    private TextView placeVicinity;
    private Button placeDetail;
    private RatingBar placeRating;
    private FloatingActionButton fabDirection;
    private LatLng lastSearchLatLng;
    private Marker markerClicked;
    private com.hku.tripals.model.Place place;
    private Boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private ImageView gps;
    private Button nearbyButton;
    private HashMap<String, Boolean> markerList = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setContentView(R.layout.activity_maps);
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_api_key));
        PlacesClient placesClient = Places.createClient(this);
        placeCard = findViewById(R.id.place_card);
        placeCard.setVisibility(View.GONE);
        placePhoto = (ImageView) findViewById(R.id.photo_imageView);
        placeName = (TextView) findViewById(R.id.place_name);
        placeVicinity = (TextView) findViewById(R.id.place_vicinity);
        placeDetail = (Button) findViewById(R.id.button_details);
        placeRating = (RatingBar) findViewById(R.id.place_ratingBar);
        fabDirection = (FloatingActionButton) findViewById(R.id.fab_direction);
        fabDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(place == null){
                    return;
                }
                Log.d(TAG, "onClick: Direct to: "+place.getName());
                Intent intent = null;
                try {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+ URLEncoder.encode(place.getName(), "utf-8")+"&destination_place_id="+place.getPlaceId()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
        Intent intent = getIntent();

        if(intent.getStringExtra("lat") != null){
            lat = intent.getStringExtra("lat");
        }
        if(intent.getStringExtra("lng") != null) {
            lng = intent.getStringExtra("lng");
        }
        if(intent.getStringExtra("type") != null){
            type = intent.getStringExtra("type");
        }
        if(type.matches("location-picker")){
            placeDetail.setText(R.string.select);
        }
        nearbyButton = findViewById(R.id.search_nearby_button);
        nearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng cameraPosition = mMap.getCameraPosition().target;
                getNearbyAttractions(cameraPosition);
            }
        });
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint(getResources().getString(R.string.query_hint));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                moveCamera(place.getLatLng(), DEFAULT_ZOOM);
                getNearbyAttractions(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        gps = (ImageView) findViewById(R.id.ic_gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    getDeviceLocation();
                }else{
                    requestLocationEnabled();
                }
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();
        if(locationPermissionGranted == true && lat.matches("") && lng.matches(""))
            requestLocationEnabled();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if(!lat.matches("") && !lng.matches("")){
            moveCamera(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), DEFAULT_ZOOM);
            getNearbyAttractions(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
        }else if(locationPermissionGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting device location");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "getDeviceLocation: locationPermissionGranted: "+locationPermissionGranted.toString());
        if(locationPermissionGranted){
            fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful() && task.getResult() != null){
                        mMap.setMyLocationEnabled(true);
                        Log.d(TAG, "onComplete: location get");
                        Location currentLocation = (Location) task.getResult();
                        Log.d(TAG, "onComplete: currentLocation "+currentLocation);
                        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        moveCamera(currentLatLng, DEFAULT_ZOOM);
                        Log.d(TAG, "onComplete: currentLatLng: "+Math.round(currentLatLng.latitude));
                        getNearbyAttractions(currentLatLng);
                    }else{
                        Log.d(TAG, "onComplete: location is null");
                        Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: ("+latLng.latitude+", "+latLng.longitude+")");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permission");
        String[] permissions = {FINE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "getLocationPermission: permission granted");
            locationPermissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: permission failed");
                    return;
                }
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    locationPermissionGranted = true;
                }
            }
        }
    }

    private void requestLocationEnabled(){
        Log.d(TAG, "requestLocationEnabled: called, check GPS on/off");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        final Activity activity = this;
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Log.d(TAG, "requestLocationEnabled: GPS is on");
                    getDeviceLocation();
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(activity,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "onActivityResult: GPS Enabled by user");
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                getDeviceLocation();
                                mMap.setMyLocationEnabled(true);
                            }
                        }, 2000);
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "onActivityResult: User rejected GPS request");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void getNearbyAttractions(LatLng latLng){
        if(lastSearchLatLng == null){
            Log.d(TAG, "getNearbyAttractions: latLng: "+latLng.toString());
            lastSearchLatLng = latLng;
            String urlString = nearbyUrlBuilder(latLng).toString();
            PlacesTask placesTask = new PlacesTask(this, mMap, markerList);
            placesTask.execute(urlString);
        }else {
            if (Math.round(lastSearchLatLng.latitude*10) != Math.round(latLng.latitude*10) ||
                    Math.round(lastSearchLatLng.longitude*10) != Math.round(latLng.longitude*10)) {
                Log.d(TAG, "getNearbyAttractions: latLng: "+latLng.toString());
                lastSearchLatLng = latLng;
                String urlString = nearbyUrlBuilder(latLng).toString();
                PlacesTask placesTask = new PlacesTask(this, mMap, markerList);
                placesTask.execute(urlString);
            }
        }
    }

    public StringBuilder nearbyUrlBuilder(LatLng location){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + location.latitude + "," + location.longitude);
        sb.append("&radius=4000");
        sb.append("&types=" + "tourist_attraction");
        sb.append("&key="+getString(R.string.place_key));
        return sb;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d(TAG, "onMarkerClick: Marker clicked");
        markerClicked = marker;
        place = (com.hku.tripals.model.Place)marker.getTag();
        placeCard.setVisibility(View.VISIBLE);
        placeName.setText(marker.getTitle());
        placeRating.setRating(Float.parseFloat(place.getRating().toString()));
        placeVicinity.setText(place.getVicinity());
        if(!place.getPhotoReference().matches("")){
            Bitmap bmp = null;
            try {
                File cachedPhoto = new File(getCacheDir(), place.getPlaceId()+".png");
                FileInputStream is = new FileInputStream(cachedPhoto);
                bmp = BitmapFactory.decodeStream(is);
                placePhoto.setVisibility(View.VISIBLE);
                placePhoto.setImageBitmap(bmp);
                Log.d(TAG, "onMarkerClick: load cached photo");
                is.close();
            } catch (Exception e) {
                Log.d(TAG, "onMarkerClick: No cached photo");
            }
            if(bmp == null) {
                placePhoto.setImageResource(R.drawable.theme_colour);
                placePhoto.setVisibility(View.VISIBLE);
                StringBuilder sbValue = new StringBuilder(photoUrlBuilder(place.getPhotoReference()));
                PhotoDownloadTask photoDownloadTask = new PhotoDownloadTask();
                photoDownloadTask.execute(sbValue.toString());
            }
        }else{
            placePhoto.setVisibility(View.GONE);
        }

        placeDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type.matches("location-picker")) {
                    Log.d(TAG, "onClick: select this place :" + place.getPlaceId());
                    Intent myIntent = new Intent();
                    myIntent.putExtra("place", (Serializable) place);
                    if (!place.getPhotoReference().matches("")) {
                        myIntent.putExtra("photo", place.getPlaceId());
                    }
                    setResult(RESULT_OK, myIntent);
                    finish();
                    MapsActivity.this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }else {
                    Log.d(TAG, "onClick: go place detail :" + place.getPlaceId());
                    Intent myIntent = new Intent(MapsActivity.this, PlaceActivity.class);
                    myIntent.putExtra("place", (Serializable) place);
                    if (!place.getPhotoReference().matches("")) {
                        myIntent.putExtra("photo", place.getPlaceId());
                    }
                    startActivity(myIntent);
                    MapsActivity.this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
            }
        });
        return false;
    }

    public StringBuilder photoUrlBuilder(String reference){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        sb.append("maxwidth="+1000);
        sb.append("&photoreference="+reference);
        sb.append("&key="+getString(R.string.place_key));
        return sb;
    }

    private Bitmap downloadPhoto(String strUrl) throws IOException {
        Log.d(TAG, "downloadPhoto: called, Url: "+strUrl);
        Bitmap bitmap=null;
        InputStream iStream = null;
        try{
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(iStream);
        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
        }
        return bitmap;
    }

    private class PhotoDownloadTask extends AsyncTask<String, Integer, Bitmap> {
        Bitmap bitmap = null;
        @Override
        protected Bitmap doInBackground(String... url) {
            try{
                bitmap = downloadPhoto(url[0]);
                try {
                    String filename = place.getPlaceId()+".png";
                    File cachePhoto = new File(getCacheDir(), filename);
                    FileOutputStream stream = new FileOutputStream(cachePhoto);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.flush();
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            placePhoto.setImageBitmap(result);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick: Map clicked");
        placeCard.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
