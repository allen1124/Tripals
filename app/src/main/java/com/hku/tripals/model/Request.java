package com.hku.tripals.model;

import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private String requestorUid;
    private String requestorName;
    private String requestorAvatar;
    private String eventId;
    private String eventTitle;
    private String eventPhotoUrl;
    private String hostUid;
    private int quota;
    private int participantSize;
    private Date timestamp;

    public Request() {
    }

    public Request(String requestorUid, String requestorName, String requestorAvatar, String eventId, String eventTitle, String eventPhotoUrl, String hostUid, int quota, int participantSize) {
        this.requestorUid = requestorUid;
        this.requestorName = requestorName;
        this.requestorAvatar = requestorAvatar;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventPhotoUrl = eventPhotoUrl;
        this.hostUid = hostUid;
        this.quota = quota;
        this.participantSize = participantSize;
    }

    public String getRequestorUid() {
        return requestorUid;
    }

    public void setRequestorUid(String requestorUid) {
        this.requestorUid = requestorUid;
    }

    public String getRequestorName() {
        return requestorName;
    }

    public void setRequestorName(String requestorName) {
        this.requestorName = requestorName;
    }

    public String getRequestorAvatar() {
        return requestorAvatar;
    }

    public void setRequestorAvatar(String requestorAvatar) {
        this.requestorAvatar = requestorAvatar;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventPhotoUrl() {
        return eventPhotoUrl;
    }

    public void setEventPhotoUrl(String eventPhotoUrl) {
        this.eventPhotoUrl = eventPhotoUrl;
    }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getParticipantSize() {
        return participantSize;
    }

    public void setParticipantSize(int participantSize) {
        this.participantSize = participantSize;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("requestorUid", requestorUid);
        result.put("requestorName", requestorName);
        result.put("requestorAvatar", requestorAvatar);
        result.put("eventId", eventId);
        result.put("eventTitle", eventTitle);
        result.put("eventPhotoUrl", eventPhotoUrl);
        result.put("hostUid", hostUid);
        result.put("quota", quota);
        result.put("participantSize", participantSize);
        result.put("timestamp", FieldValue.serverTimestamp());
        return result;
    }
}
