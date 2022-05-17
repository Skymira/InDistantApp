package com.example.indistant.models;

public class ModelUsers {

    // Use same name as in firebase database
    String username, email, search, country, distance, image, cover, uid, status, typingTo, birthday, gender, name;
    boolean isBlocked = false;
    public ModelUsers() {
    }

    public ModelUsers(String username, String email, String search, String country, String distance, String image, String cover, String uid, String status, String typingTo, String birthday, String gender, String name, boolean isBlocked) {
        this.username = username;
        this.email = email;
        this.search = search;
        this.country = country;
        this.distance = distance;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.status = status;
        this.typingTo = typingTo;
        this.birthday = birthday;
        this.gender = gender;
        this.name = name;
        this.isBlocked = isBlocked;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
