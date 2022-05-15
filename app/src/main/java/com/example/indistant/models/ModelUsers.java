package com.example.indistant.models;

public class ModelUsers {

    // Use same name as in firebase database
    String username, email, search, country, distance, image, cover, uid;

    public ModelUsers() {
    }

    public ModelUsers(String username, String email, String search, String country, String distance, String image, String cover, String uid) {
        this.username = username;
        this.email = email;
        this.search = search;
        this.country = country;
        this.distance = distance;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
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

    public String getCountries() {
        return country;
    }

    public void setCountries(String countries) {
        this.country = countries;
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
}
