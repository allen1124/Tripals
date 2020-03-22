package com.hku.tripals.model;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String uid;
    private String displayName;
    private String avatarImageUrl;
    private String gender;
    private String birthday;
    private String homeCountry;
    private String language;
    private String bio;
    private List<String> interests;


    public User() {
    }

    public User(String uid, String gender, String birthday, String homeCountry, String language, String bio){
        this.uid = uid;
        this.gender = gender;
        this.birthday = birthday;
        this.homeCountry = homeCountry;
        this.language = language;
        this.bio = bio;
    }


    public User(String uid, String gender, String birthday, String homeCountry, String language, String bio, String avatarImageUrl){
        this.uid = uid;
        this.gender = gender;
        this.birthday = birthday;
        this.homeCountry = homeCountry;
        this.language = language;
        this.bio = bio;
        this.avatarImageUrl = avatarImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHomeCountry() {
        return homeCountry;
    }

    public void setHomeCountry(String homeCountry) {
        this.homeCountry = homeCountry;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("displayName", displayName);
        result.put("avatarImageUrl", avatarImageUrl);
        result.put("gender", gender);
        result.put("birthday", birthday);
        result.put("homeCountry", homeCountry);
        result.put("language", language);
        result.put("bio", bio);
        result.put("interests", interests);
        return result;
    }
}
