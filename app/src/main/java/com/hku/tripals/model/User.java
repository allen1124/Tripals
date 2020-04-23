package com.hku.tripals.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private String uid;
    private String displayName;
    private String avatarImageUrl;
    private String gender;
    private String birthday;
    private String homeCountry;
    private String language;
    private String bio;
    private List<String> interests;
    private String facebook;
    private String instagram;


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

    public User(String uid, String gender, String birthday, String homeCountry, String language, String bio, String avatarImageUrl, String facebook, String instagram){
        this.uid = uid;
        this.gender = gender;
        this.birthday = birthday;
        this.homeCountry = homeCountry;
        this.language = language;
        this.bio = bio;
        this.avatarImageUrl = avatarImageUrl;
        this.facebook = facebook;
        this.instagram = instagram;
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

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public List<String> getInterests() {
        return interests;
    }

    /*public String getInterestsstring() {
        String intereststring = "";
        for (int i=0; i<interests.size(); i++){
            intereststring = intereststring + interests[i].toString();
            if(i != interests.size() -1) {
                intereststring = intereststring + ", ";
            }
        }
        return intereststring;
    }*/

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
        result.put("facebook", facebook);
        result.put("instagram", instagram);

        return result;
    }
}