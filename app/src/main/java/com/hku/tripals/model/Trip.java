package com.hku.tripals.model;

import com.google.firebase.firestore.FieldValue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trip implements Serializable {

    private String id;
    private String host;
    private String hostName;
    private String hostAvatarUrl;
    private String title;
    private String destination;
    private String photoUrl;

    private List<String> events;
    private Date timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAvatarUrl() {
        return hostAvatarUrl;
    }

    public void setHostAvatarUrl(String hostAvatarUrl) {
        this.hostAvatarUrl = hostAvatarUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("host", host);
        result.put("hostName", hostName);
        result.put("hostAvatarUrl", hostAvatarUrl);
        result.put("title", title);
        result.put("destination", destination);
        result.put("photoUrl", photoUrl);
        result.put("events", events);
        result.put("timestamp", FieldValue.serverTimestamp());
        return result;
    }
}
