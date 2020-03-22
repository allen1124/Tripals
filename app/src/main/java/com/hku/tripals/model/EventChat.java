package com.hku.tripals.model;

import java.util.List;

public class EventChat {
    private String eventId;
    private String eventPhotoUrl;
    private String eventTitle;
    private String host;
    private String lastestMsg;
    private List<String> participants;

    public EventChat(){
    }

    public EventChat(String eventId, String eventPhotoUrl, String eventTitle, String host, List<String> participants) {
        this.eventId = eventId;
        this.eventPhotoUrl = eventPhotoUrl;
        this.eventTitle = eventTitle;
        this.host = host;
        this.participants = participants;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventPhotoUrl() {
        return eventPhotoUrl;
    }

    public void setEventPhotoUrl(String eventPhotoUrl) {
        this.eventPhotoUrl = eventPhotoUrl;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
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

    public String getLastestMsg() {
        return lastestMsg;
    }

    public void setLastestMsg(String lastestMsg) {
        this.lastestMsg = lastestMsg;
    }
}