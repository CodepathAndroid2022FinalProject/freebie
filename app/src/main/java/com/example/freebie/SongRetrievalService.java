package com.example.freebie;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.freebie.models.Album;
import com.example.freebie.models.Artist;
import com.example.freebie.models.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;

public class SongRetrievalService {

    public static final String TAG = "SongRetrievalService";

    public static final String BASE_URL = "https://api.discogs.com/database/";
    public static final String API_KEY = "DHqvPIxOJwmIMQHtHkkoewRydAnSCRwFXnNAOUCI";

    public static final String SONGS_LOADED = "songs_loaded";
    public static final String SIZE_KEY = "size";

    public static SongRetrievalService songRetrievalService;
    private static Context context;

    public static boolean loadingSongs = false;

    private static SharedPreferences sharedPreferences;

    public SongRetrievalService(Context context) { SongRetrievalService.context = context; }

    public static SongRetrievalService getInstance(Context context) {
        SongRetrievalService.context = context;
        if(songRetrievalService == null)
            songRetrievalService = new SongRetrievalService(context);
        return songRetrievalService;
    }

    public void getSongs() {
        // Check if previously loaded songs are still in memory
        sharedPreferences = context.getSharedPreferences(SONGS_LOADED, Context.MODE_PRIVATE);
        int previousListSize = sharedPreferences.getInt(SIZE_KEY, 0);
        if (previousListSize == Song.songArrayList.size() && Song.songArrayList.size() != 0)
            return;

        Log.i(TAG, "Getting songs from disk!");
        loadingSongs = true;

        // Remove stale data
        Song.songArrayList.clear();
        Album.albumArrayList.clear();
        Artist.artistArrayList.clear();

        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri filePathUri;
        String filePath = "Unknown";
        Cursor songCursor = context.getContentResolver().query(songUri, null, null, null, null);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitleIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtistIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbumIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int songLengthIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                // Retrieve song path
                filePathUri = Uri.parse(songCursor.getString(songLengthIndex));
                filePath = filePathUri.getPath();

                // Set the working file
                mediaMetadataRetriever.setDataSource(filePath);

                // Retrieve title, artist, and album
                String titleString = songCursor.getString(songTitleIndex);
                String artistString = songCursor.getString(songArtistIndex);
                String albumString = songCursor.getString(songAlbumIndex);

                // Retrieve song length
                String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long rawLength = Long.parseLong(duration);
                String seconds = String.valueOf((rawLength % 60000) / 1000);
                String minutes = String.valueOf(rawLength / 60000);
                String length;
                if (seconds.length() == 1)
                    length = minutes + ":" + "0" + seconds;
                else
                    length = minutes + ":" + seconds;

                // Find unique artists to add into artist collection
                if (Artist.artistArrayList.size() == 0) {
                    getArtistDetails(artistString);
                } else {
                    for (int i = 0; i < Artist.artistArrayList.size(); i++) {
                        if (Artist.artistArrayList.get(i).getName().equals(artistString))
                            break;
                        if(i == Artist.artistArrayList.size() - 1) {
                            getArtistDetails(artistString);
                        }
                    }
                }

                // Find unique albums to add into album collection and decode relevant album art
                Bitmap highResAlbumBitmap = null;
                Bitmap lowResAlbumBitmap = null;
                if (Album.albumArrayList.size() == 0) {
                    highResAlbumBitmap = loadFullResAlbumArt(mediaMetadataRetriever, highResAlbumBitmap);
                    lowResAlbumBitmap = loadLowResAlbumArt(mediaMetadataRetriever, lowResAlbumBitmap);
                    Album albumItem = new Album(albumString, artistString, highResAlbumBitmap, lowResAlbumBitmap);
                    Album.albumArrayList.add(albumItem);
                } else {
                    for (int i = 0; i < Album.albumArrayList.size(); i++) {
                        if (Album.albumArrayList.get(i).getTitle().equals(albumString))
                            break;
                        if(i == Album.albumArrayList.size() - 1) {
                            highResAlbumBitmap = loadFullResAlbumArt(mediaMetadataRetriever, highResAlbumBitmap);
                            lowResAlbumBitmap = loadLowResAlbumArt(mediaMetadataRetriever, lowResAlbumBitmap);
                            Album albumItem = new Album(albumString, artistString, highResAlbumBitmap, lowResAlbumBitmap);
                            Album.albumArrayList.add(albumItem);
                        }
                    }
                }

                // Prevent decoding the same bitmap multiple times
                Album songAlbum = null;
                for(int i = 0; i < Album.albumArrayList.size(); i++) {
                    if (Album.albumArrayList.get(i).getTitle().equals(albumString)) {
                        songAlbum = Album.albumArrayList.get(i);
                        break;
                    }
                }
                Song song = new Song(titleString, artistString, songAlbum, length, filePath);
                Song.songArrayList.add(song);
            } while (songCursor.moveToNext());
        }
        Log.i(TAG, "Parsing for songs finished with " + Song.songArrayList.size() + " total songs!");
        songCursor.close();
        mediaMetadataRetriever.release();
        loadingSongs = false;

        sharedPreferences = context.getSharedPreferences(SONGS_LOADED, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putInt(SIZE_KEY, Song.songArrayList.size());

        myEdit.apply();
    }

    private Bitmap loadLowResAlbumArt(MediaMetadataRetriever mediaMetadataRetriever, Bitmap bitmap) {
        byte[] songAlbumArtData = mediaMetadataRetriever.getEmbeddedPicture();
        if (songAlbumArtData != null) {
            bitmap = BitmapFactory.decodeByteArray(songAlbumArtData,
                    0, songAlbumArtData.length);
            bitmap = Bitmap.createScaledBitmap(bitmap,
                    100, 100, false);
        }
        return bitmap;
    }

    private Bitmap loadFullResAlbumArt(MediaMetadataRetriever mediaMetadataRetriever, Bitmap bitmap) {
        byte[] songAlbumArtData = mediaMetadataRetriever.getEmbeddedPicture();
        if (songAlbumArtData != null) {
            bitmap = BitmapFactory.decodeByteArray(songAlbumArtData,
                    0, songAlbumArtData.length);
        }
        return bitmap;
    }

    private void getArtistDetails(String artistString) {
        // Put together elements of request URL
        String artistNameURL = artistString.replaceAll(" ", "%20");
        String requestURL = BASE_URL + "search?q=" + artistNameURL + "&per_page=1"
                + "&token=" + API_KEY;

        Artist artistItem = new Artist(artistString, null);
        Artist.artistArrayList.add(artistItem);

        int currentSongIndex = 0;
        if(Artist.artistArrayList.size() > 1)
            currentSongIndex = Artist.artistArrayList.size() - 1;

        AsyncHttpClient client = new AsyncHttpClient();
        int finalCurrentSongIndex = currentSongIndex;
        client.get(requestURL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                String artistPicture = null;
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray results = jsonObject.getJSONArray("results");
                    JSONObject artistData = results.getJSONObject(0);
                    artistPicture = artistData.getString("cover_image");
                    if (artistPicture.contains(".gif"))
                        artistPicture = null;
                } catch (JSONException e) {
                    Log.e(TAG, "Hit json exception", e);
                    e.printStackTrace();
                    Artist.artistArrayList.get(finalCurrentSongIndex).setProfilePicture(null);
                    return;
                }
                Artist.artistArrayList.get(finalCurrentSongIndex).setProfilePicture(artistPicture);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Failed!");
                Log.d(TAG, "" + statusCode);
                Log.d(TAG, response);
                Log.d(TAG, throwable.getMessage());
            }
        });
    }
}
