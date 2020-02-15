package com.hku.tripals.model;

import com.google.android.gms.maps.model.LatLng;

public class Destination {

    private int id;
    private int image;
    private int name;
    private String latitude;
    private String longitude;

    public Destination() {
        super();
    }

    public Destination(int id, int image, int name, String latitude, String longitude) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
