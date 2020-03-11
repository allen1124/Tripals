package com.hku.tripals.model;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {

    private String id;
    private String host;
    private String hostName;
    private String hostAvatarUrl;
    private List<String> participants;
    private String title;
    private String description;
    private String privacy;
    private Date datetime;
    private String location;
    private String locationName;
    private String photoUrl;
    private List<String> interests;
    private @ServerTimestamp FieldValue timestamp = FieldValue.serverTimestamp();

    public Event() {
    }

    public Event(String id, String host, String hostAvatarUrl, List<String> participants, String title, String description, Date datetime, String location, String photoUrl, List<String> interests) {
        this.id = id;
        this.host = host;
        this.hostAvatarUrl = hostAvatarUrl;
        this.participants = participants;
        this.title = title;
        this.description = description;
        this.datetime = datetime;
        this.location = location;
        this.photoUrl = photoUrl;
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

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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
        result.put("hostAvatarUrl", hostAvatarUrl);
        result.put("hostName", hostName);
        result.put("participants", participants);
        result.put("title", title);
        result.put("description", description);
        result.put("privacy", privacy);
        result.put("datetime", datetime);
        result.put("location", location);
        result.put("locationName", locationName);
        result.put("photoUrl", photoUrl);
        result.put("interests", interests);
        result.put("timestamp", timestamp);
        return result;
    }
}
