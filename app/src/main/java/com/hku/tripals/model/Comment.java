package com.hku.tripals.model;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Comment {

    String cid;
    String userId;
    String username;
    String userPhoto;
    String comment;
    String timestamp;

    public Comment() {
    }

    public Comment(String cid, String userId, String username, String userPhoto, String comment, String timestamp) {
        this.cid = cid;
        this.userId = userId;
        this.username = username;
        this.userPhoto = userPhoto;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("cid", cid);
        result.put("userId", userId);
        result.put("username", username);
        result.put("userPhoto", userPhoto);
        result.put("comment", comment);
        result.put("timestamp", timestamp);
        return result;
    }
}
