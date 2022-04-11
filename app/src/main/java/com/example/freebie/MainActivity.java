package com.example.freebie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.freebie.fragments.AlbumsFragment;
import com.example.freebie.fragments.ArtistsFragment;
import com.example.freebie.fragments.HomeFragment;
import com.example.freebie.fragments.SettingsFragment;
import com.example.freebie.models.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    public static MainActivity mainActivity;

    public static MediaPlayer mediaPlayer;
    public static Song currentlyPlayingSong;

    public HomeFragment homeFragment;
    public AlbumsFragment albumsFragment;
    public ArtistsFragment artistsFragment;
    public SettingsFragment settingsFragment;
    private GetSongsCompleteListener getSongsCompleteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        mediaPlayer = new MediaPlayer();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Create each fragment in advance
        homeFragment = new HomeFragment();
        albumsFragment = new AlbumsFragment();
        artistsFragment = new ArtistsFragment();
        settingsFragment = new SettingsFragment();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                String fragmentTag = null;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = homeFragment;
                        fragmentTag = "HomeFragment";
                        break;
                    case R.id.action_albums:
                        fragment = albumsFragment;
                        fragmentTag = "AlbumsFragment";
                        break;
                    case R.id.action_artists:
                        fragment = artistsFragment;
                        fragmentTag = "ArtistsFragment";
                        break;
                    case R.id.action_settings:
                        fragment = settingsFragment;
                        fragmentTag = "SettingsFragment";
                        break;
                    default:
                        fragment = homeFragment;
                        fragmentTag = "HomeFragment";
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, fragmentTag).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        // Set currently playing song to nothing when song completes
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) { currentlyPlayingSong = null; }
        });

        getSongsFromDB();
    }

    public void getSongsComplete() {
        SongDatabase songDatabase = SongDatabase.instanceOfDataBase(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Work to do
                Log.i(TAG, "Filling database...");
                songDatabase.getSongsFromFS();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Filled DB!");
                        getSongsFromDB();
                    }
                });
            }
        }).start();

        // Signal that the read is complete
        Log.i(TAG, "Loading complete!");
    }

    public void getSongsFromDB() {
        SongDatabase songDatabase = SongDatabase.instanceOfDataBase(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Work to do
                Log.i(TAG, "Filling song array...");
                songDatabase.getSongsFromDB();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Signal that loading is complete
                        Log.i(TAG, "Filled song array!");
                    }
                });
            }
        }).start();
    }

    public void setGetSongsCompleteListener(GetSongsCompleteListener listener) {
        this.getSongsCompleteListener = listener;
    }
}