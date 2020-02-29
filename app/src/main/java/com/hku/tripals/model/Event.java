package com.hku.tripals.model;

import java.util.List;

public class Event {

    private String id;
    private String title;
    private String description;
    private String datetime;
    private String location;
    private List<String> interests;

    public Event() {
    }

    public Event(String id, String title, String description, String datetime, String location, List<String> interests) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.datetime = datetime;
        this.location = location;
        this.interests = interests;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }
}
