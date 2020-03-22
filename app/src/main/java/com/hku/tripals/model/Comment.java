package com.hku.tripals.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Comment {

    String userId;
    String username;
    String userPhoto;
    String comment;
    String commentPhoto;
    Date timestamp;

    public Comment() {
    }

    public Comment(String userId, String username, String userPhoto, String comment) {
        this.userId = userId;
        this.username = username;
        this.userPhoto = userPhoto;
        this.comment = comment;
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

    public String getCommentPhoto() {
        return commentPhoto;
    }

    public void setCommentPhoto(String commentPhoto) {
        this.commentPhoto = commentPhoto;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("userPhoto", userPhoto);
        result.put("comment", comment);
        result.put("commentPhoto", commentPhoto);
        result.put("timestamp", FieldValue.serverTimestamp());
        return result;
    }
}
