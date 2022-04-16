package com.example.freebie;

import static com.example.freebie.MainActivity.mainActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.freebie.models.Song;

public class SongRetrievalService {

    public static final String TAG = "SongRetrievalService";
    public static SongRetrievalService songRetrievalService;
    private static Context context;

    public static boolean loadingSongs = false;

    public SongRetrievalService(Context context) { SongRetrievalService.context = context; }

    public static SongRetrievalService getInstance(Context context) {
        SongRetrievalService.context = context;
        if(songRetrievalService == null)
            songRetrievalService = new SongRetrievalService(context);
        return songRetrievalService;
    }

    public void getSongs() {
        Log.i(TAG, "Getting songs from disk!");
        loadingSongs = true;
        Song.songArrayList.clear();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri filePathUri;
        String filePath = "Unknown";
        Cursor songCursor = context.getContentResolver().query(songUri, null, null, null, null);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        if(songCursor != null && songCursor.moveToFirst()) {
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
                String title = songCursor.getString(songTitleIndex);
                String artist = songCursor.getString(songArtistIndex);
                String album = songCursor.getString(songAlbumIndex);

                // Retrieve song length
                String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long rawLength = Long.parseLong(duration);
                String seconds = String.valueOf((rawLength % 60000) / 1000);
                String minutes = String.valueOf(rawLength / 60000);
                String length;
                if(seconds.length() == 1)
                    length = minutes + ":" + "0" + seconds;
                else
                    length = minutes + ":" + seconds;

                // Retrieve album art
                Bitmap albumBitmap = null;
                byte[] albumArtData = mediaMetadataRetriever.getEmbeddedPicture();

                if (albumArtData != null) {
                    albumBitmap = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);
                    albumBitmap = Bitmap.createScaledBitmap(albumBitmap, 128, 128, false);
                }

                // Create song model and add to static array
                Song song = new Song(title, artist, album, length, filePath, albumBitmap);
                Song.songArrayList.add(song);
                Log.i(TAG, "Current songArrayList size - " + Song.songArrayList.size());
            } while (songCursor.moveToNext());
        }
        Log.i(TAG, "Parsing for songs finished with " + Song.songArrayList.size() + " total songs!");
        songCursor.close();
        mediaMetadataRetriever.release();
        loadingSongs = false;
    }
}
