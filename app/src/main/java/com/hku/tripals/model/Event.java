package com.hku.tripals.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {

    private String id;
    private String host;
    private List<String> participants;
    private String title;
    private String description;
    private Date datetime;
    private String location;
    private String locationPhotoUrl;
    private List<String> interests;

    public Event() {
    }

    public Event(String id, String host, List<String> participants, String title, String description, Date datetime, String location, String locationPhotoUrl, List<String> interests) {
        this.id = id;
        this.host = host;
        this.participants = participants;
        this.title = title;
        this.description = description;
        this.datetime = datetime;
        this.location = location;
        this.locationPhotoUrl = locationPhotoUrl;
        this.interests = interests;
    }

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

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationPhotoUrl() {
        return locationPhotoUrl;
    }

    public void setLocationPhotoUrl(String locationPhotoUrl) {
        this.locationPhotoUrl = locationPhotoUrl;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("host", host);
        result.put("participants", participants);
        result.put("title", title);
        result.put("description", description);
        result.put("datetime", datetime);
        result.put("location", location);
        result.put("locationPhotoUrl", locationPhotoUrl);
        result.put("interests", interests);
        return result;
    }
}
