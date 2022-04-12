package com.example.freebie.models;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Song {

    public static ArrayList<Song> songArrayList = new ArrayList<>();

    private String title;
    private String artist;
    private String album;
    private String length;
    private String path;
    private Bitmap albumArt;

    public Song(String title, String artist, String album, String length, String path, Bitmap albumArt){
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.path = path;
        this.albumArt = albumArt;
    }

    public String getTitle() { return title; }

    public String getArtist() { return artist; }

    public String getAlbum() { return album; }

    public String getLength() { return length; }

    public String getPath() { return path; }

    public Bitmap getAlbumArt() { return albumArt; }
    public void setAlbumArt(Bitmap albumArt) { this.albumArt = albumArt; }
}
