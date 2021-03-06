package com.hku.tripals.model;

import java.util.List;

public class EventChat {
    private String eventId;
    private String eventPhotoUrl;
    private String eventTitle;
    private String host;
    private String lastestMsg;
    private String type;
    private List<String> participants;
    private List<String> participantName;
    private List<String> participantPhotoUrl;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getParticipantName() {
        return participantName;
    }

    public void setParticipantName(List<String> participantName) {
        this.participantName = participantName;
    }

    public List<String> getParticipantPhotoUrl() {
        return participantPhotoUrl;
    }

    public void setParticipantPhotoUrl(List<String> participantPhotoUrl) {
        this.participantPhotoUrl = participantPhotoUrl;
    }
}