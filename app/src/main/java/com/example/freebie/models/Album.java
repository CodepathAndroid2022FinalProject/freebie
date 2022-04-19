package com.example.freebie.models;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Album {

    public static ArrayList<Album> albumArrayList = new ArrayList<>();

    private String title;
    private String artist;
    private Bitmap albumArt;

    public Album(String title, String artist, Bitmap albumArt){
        this.title = title;
        this.artist = artist;
        this.albumArt = albumArt;
    }

    public String getTitle() { return title; }

    public String getArtist() { return artist; }

    public Bitmap getAlbumArt() { return albumArt; }
}
