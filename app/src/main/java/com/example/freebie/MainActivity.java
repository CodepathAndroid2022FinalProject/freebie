package com.example.freebie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.freebie.fragments.AlbumsFragment;
import com.example.freebie.fragments.ArtistsFragment;
import com.example.freebie.fragments.HomeFragment;
import com.example.freebie.fragments.SettingsFragment;
import com.example.freebie.models.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sothree.slidinguppanel.PanelState;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    public static MainActivity mainActivity;

    public HomeFragment homeFragment;
    public AlbumsFragment albumsFragment;
    public ArtistsFragment artistsFragment;
    public SettingsFragment settingsFragment;

    public SlidingUpPanelLayout panelLayout;
    public Button btnPlay;

    private static boolean gettingSongs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        Log.i(TAG, "Reloading MainActivity");
        if(!gettingSongs) {
            Thread GettingSongsFromDisk = new Thread(new Runnable() {
                @Override
                public void run() {
                    gettingSongs = true;
                    SongRetrievalService songRetrievalService = SongRetrievalService.getInstance(getApplicationContext());
                    songRetrievalService.getSongs();
                    gettingSongs = false;
                }
            });
            GettingSongsFromDisk.start();
        }

        MediaPlayerService mediaPlayerService = MediaPlayerService.getInstance(getApplicationContext());

        panelLayout = findViewById(R.id.sliding_layout);
        btnPlay = findViewById(R.id.btnPlay);

        panelLayout.setPanelState(PanelState.HIDDEN);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MediaPlayerService.mediaPlayer.isPlaying()) {
                    MediaPlayerService.mediaPlayer.pause();
                    btnPlay.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
                } else {
                    MediaPlayerService.mediaPlayer.start();
                    btnPlay.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
                }
            }
        });

        // Create each fragment in advance
        homeFragment = new HomeFragment();
        albumsFragment = new AlbumsFragment();
        artistsFragment = new ArtistsFragment();
        settingsFragment = new SettingsFragment();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
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
                fragmentManager.beginTransaction()
                        .setCustomAnimations(
                                R.anim.fade_in,
                                R.anim.fade_out
                        )
                        .replace(R.id.flContainer, fragment, fragmentTag).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }
}