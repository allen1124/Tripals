package com.hku.tripals.model;

import java.util.List;

public class User {
    private String uid;
    private String avatarImageUrl;
    private String gender;
    private String birthday;
    private String homeCountry;
    private String language;
    private String bio;

    private String displayName;
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

    //uid

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    //gender

    public String getgender() {
        return gender;
    }

    public void setgender(String uid) {
        this.gender = gender;
    }

    //birthday

    public String getbirthday() {
        return birthday;
    }

    public void setbirthday(String uid) {
        this.birthday= birthday;
    }

    //homeCountry

    public String gethomeCountry() {
        return homeCountry;
    }

    public void sethomeCountry(String uid) {
        this.homeCountry = homeCountry;
    }

    //language

    public String getlanguage() {
        return language;
    }

    public void setlanguage(String uid) {
        this.language = language;
    }

    //bio

    public String getbio() {
        return bio;
    }

    public void setbio(String uid) {
        this.language = bio;
    }


    //interest

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    //AvatarImageUr

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUser () {
        String s = "";
        s += "gender" + gender;
        s += ", Home Country" + homeCountry;
        s += ", birthday" + birthday;
        s += ", bio" + bio;
        return s;
    }
}
