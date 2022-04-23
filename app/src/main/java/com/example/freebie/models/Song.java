package com.example.freebie.models;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Song {

    public static ArrayList<Song> songArrayList = new ArrayList<>();

    private String title;
    private String artist;
    private Album album;
    private String length;
    private String path;

    public Song(String title, String artist, Album album, String length, String path){
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.path = path;
    }

    public String getTitle() { return title; }

    public String getArtist() { return artist; }

    public Album getAlbum() { return album; }

    public String getLength() { return length; }

    public String getPath() { return path; }
}
