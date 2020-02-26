package com.hku.tripals;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.hku.tripals.task.PlacesTask;

import java.util.Arrays;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private String lat, lng;
    private LatLng lastSearchLatLng;
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
        Intent intent = getIntent();
        lat = intent.getStringExtra("lat");
        lng = intent.getStringExtra("lng");
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
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
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

    public StringBuilder photoUrlBuilder(String reference){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        sb.append("maxwidth="+1000);
        sb.append("&photoreference="+reference);
        sb.append("&key="+getString(R.string.place_key));
        return sb;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
