package com.stream.jerye.queue.lobby;

/**
 * Created by jerye on 7/8/2017.
 */

public class User {
    private String spotifyProfileId;
    private String registrationToken;
    private String name;
    private String picture;

    public User() {
    }

    public User(String name, String spotifyProfileId, String registrationToken, String picture) {
        this.spotifyProfileId = spotifyProfileId;
        this.registrationToken = registrationToken;
        this.name = name;
        this.picture = picture;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture() {
        this.picture = picture;
    }

    public String getSpotifyProfileId() {
        return spotifyProfileId;
    }

    public void setSpotifyProfileId(String spotifyProfileId) {
        this.spotifyProfileId = spotifyProfileId;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String title) {
        this.registrationToken = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
