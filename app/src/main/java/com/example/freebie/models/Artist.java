package com.example.freebie.models;

import java.util.ArrayList;

public class Artist {

    public static ArrayList<Artist> artistArrayList = new ArrayList<>();

    private String name;
    private String profilePicture;

    public Artist(String artist, String profilePicture){
        this.name = artist;
        this.profilePicture = profilePicture;
    }

    public String getName() { return name; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}
