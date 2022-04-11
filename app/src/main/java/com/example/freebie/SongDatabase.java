package com.example.freebie;

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
import android.view.View;

import androidx.fragment.app.Fragment;

import com.example.freebie.models.Song;

import java.io.File;
import java.util.ArrayList;

public class SongDatabase extends SQLiteOpenHelper {

    public static final String TAG = "DB";

    private static SongDatabase songDatabase;
    private static Context context;

    private static final String DATABASE_NAME = "SongDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Songs";
    private static final String COUNTER = "Counter";

    private static final String TITLE_FIELD = "title";
    private static final String ARTIST_FIELD = "artist";
    private static final String ALBUM_FIELD = "album";
    private static final String LENGTH_FIELD = "length";
    private static final String PATH_FIELD = "path";

    private DiskSongQueryListener diskListener;
    private DBSongQueryListener dbListener;

    public SongDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SongDatabase instanceOfDataBase(Context context) {
        SongDatabase.context = context;
        if(songDatabase == null)
            songDatabase = new SongDatabase(context);
        return songDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append("(")
                .append(COUNTER)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(TITLE_FIELD)
                .append(" TEXT, ")
                .append(ARTIST_FIELD)
                .append(" TEXT, ")
                .append(ALBUM_FIELD)
                .append(" TEXT, ")
                .append(LENGTH_FIELD)
                .append(" TEXT, ")
                .append(PATH_FIELD)
                .append(" TEXT)");

        sqLiteDatabase.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Create case statement here if you need to upgrade the database across
        // app versions for whatever reason
    }

    public void fillSongListAsync() {
        Thread tFillDatabase = new Thread(new Runnable() {
            @Override
            public void run() {
                // Work to do
                Log.i(TAG, "Filling database...");
                populateSongListArray();
            }
        });

        tFillDatabase.start();
        try {
            tFillDatabase.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Database filled!");
        dbListener.onCompletion();
    }

    public void populateSongListArray() {
        Song.songArrayList.clear();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        try (Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if(cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    // Collect info from db
                    String title = cursor.getString(1);
                    String artist = cursor.getString(2);
                    String album = cursor.getString(3);
                    String length = cursor.getString(4);
                    String path = cursor.getString(5);

                    // Create song model and add to static array
                    Song song = new Song(title, artist, album, length, path);
                    Song.songArrayList.add(song);
                }
            }
        }
    }

    public void fillDBAsync() {
        Thread tFillDatabase = new Thread(new Runnable() {
            @Override
            public void run() {
                // Work to do
                Log.i(TAG, "Filling database...");
                getSongsFromFS();
            }
        });

        tFillDatabase.start();
        try {
            tFillDatabase.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        diskListener.onCompletion();
    }

    public void getSongsFromFS() {
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

//                // Get album art and convert it to a bitmap
//                Bitmap albumBitmap = null;
//                byte[] albumArtData = mediaMetadataRetriever.getEmbeddedPicture();
//
//                if (albumArtData != null) {
//                    albumBitmap = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);
//                    albumBitmap = Bitmap.createScaledBitmap(albumBitmap, 128, 128, false);
//                }

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

                SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(TITLE_FIELD, title);
                contentValues.put(ARTIST_FIELD, artist);
                contentValues.put(ALBUM_FIELD, album);
                contentValues.put(LENGTH_FIELD, length);
                contentValues.put(PATH_FIELD, filePath);

                // TODO: Find way to avoid duplicates on reload
                sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

                Log.i(TAG, "Song - " + title + " - has just been added");
            } while (songCursor.moveToNext());
        }
        songCursor.close();
        mediaMetadataRetriever.release();

        Log.i(TAG, "Database filled!");
    }

    public void setDiskSongQueryListener(DiskSongQueryListener listener) {
        this.diskListener = listener;
    }

    public void setDBSongQueryListener(DBSongQueryListener listener) {
        this.dbListener = listener;
    }
}
