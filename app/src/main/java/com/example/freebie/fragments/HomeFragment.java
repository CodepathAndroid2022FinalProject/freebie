package com.example.freebie.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.freebie.R;
import com.example.freebie.SongDatabase;
import com.example.freebie.SongsAdapter;
import com.example.freebie.models.Song;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private RecyclerView rvSongs;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<Song> allSongs;
    private SongsAdapter adapter;

    private FragmentManager fragmentManager;

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

        fragmentManager = getParentFragmentManager();

        rvSongs = view.findViewById(R.id.rvSongs);
        progressBar = view.findViewById(R.id.progressBar);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        allSongs = new ArrayList<>();
        adapter = new SongsAdapter(getContext(), allSongs);

        rvSongs.setAdapter(adapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Force refresh songs found on disk
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SongDatabase songDatabase = SongDatabase.instanceOfDataBase(getContext());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        songDatabase.getSongsFromFS();
                        songDatabase.getSongsFromDB();

                        // Avoid crash if changing tab while refreshing
                        Fragment fragment = fragmentManager.findFragmentByTag(TAG);
                        if(fragment == null) {
                            Log.w(TAG, "Breaking out of thread, fragment switched during loading");
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshSongs();
                                swipeContainer.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });
        refreshSongs();
    }

    private void refreshSongs() {
        // Remember to CLEAR OUT old items before appending in the new ones
        adapter.clear();
        adapter.addAll(Song.songArrayList);

        // Signal refresh has finished
        adapter.notifyDataSetChanged();
        // Disable loading bar when ready
        progressBar.setVisibility(View.GONE);
    }
}