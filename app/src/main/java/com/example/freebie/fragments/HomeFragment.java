package com.example.freebie.fragments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.freebie.DBSongQueryListener;
import com.example.freebie.DiskSongQueryListener;
import com.example.freebie.R;
import com.example.freebie.SongDatabase;
import com.example.freebie.SongsAdapter;
import com.example.freebie.models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private RecyclerView rvSongs;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<Song> allSongs = new ArrayList<>();
    private SongsAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSongs = view.findViewById(R.id.rvSongs);
        progressBar = view.findViewById(R.id.progressBar);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        adapter = new SongsAdapter(getContext(), allSongs);

        rvSongs.setAdapter(adapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        SongDatabase songDatabase = SongDatabase.instanceOfDataBase(getContext());
        songDatabase.setDBSongQueryListener(new DBSongQueryListener() {
            @Override
            public void onCompletion() {
                refreshSongs();
                progressBar.setVisibility(View.GONE);
            }
        });
        songDatabase.fillSongListAsync();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // This needs to refresh the database with new info from disk
                // but when the time comes, all other fragments must be notified as well
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void refreshSongs() {
        // Remember to CLEAR OUT old items before appending in the new ones
        adapter.clear();
        allSongs.addAll(Song.songArrayList);

        // Signal refresh has finished
        adapter.notifyDataSetChanged();
        // Disable loading bar when ready
        progressBar.setVisibility(View.GONE);
    }
}