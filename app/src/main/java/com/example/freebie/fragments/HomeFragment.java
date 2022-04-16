package com.example.freebie.fragments;

import static com.example.freebie.MainActivity.mainActivity;

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
import com.example.freebie.SongRetrievalService;
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

        progressBar.setVisibility(View.VISIBLE);

        // Force refresh songs found on disk
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Don't start refreshing songs again while a refresh is already happening
                if(SongRetrievalService.loadingSongs) {
                    swipeContainer.setRefreshing(false);
                    return;
                }

                Thread GettingSongsFromDisk = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SongRetrievalService songRetrievalService = SongRetrievalService.getInstance(getContext());
                        songRetrievalService.getSongs();
                    }
                });
                GettingSongsFromDisk.start();
                refreshSongs();
                swipeContainer.setRefreshing(false);
            }
        });
        refreshSongs();
    }

    public void refreshSongs() {
        Log.i(TAG, "Rebuilding list!");
        // Remember to CLEAR OUT old items before appending in the new ones
        adapter.clear();
        SongRetrievalService songRetrievalService = SongRetrievalService.getInstance(getContext());
        Thread RefreshingHomeFragment = new Thread(new Runnable() {
            @Override
            public void run() {
                while(songRetrievalService.loadingSongs) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Avoid crash if changing tab while refreshing
                    Fragment fragment = fragmentManager.findFragmentByTag(TAG);
                    if(fragment == null) {
                        Log.w(TAG, "Breaking out of thread, fragment switched during loading");
                        return;
                    }

                    int startSize = adapter.songs.size();
                    int endSize = Song.songArrayList.size();
                    if(startSize < endSize) {
                        for (int i = startSize; i < endSize; i++)
                            adapter.add(Song.songArrayList.get(i));

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i = startSize; i < endSize; i++)
                                    adapter.notifyItemInserted(i);
                                if(adapter.songs.size() > 0 && progressBar.getVisibility() == View.VISIBLE)
                                    progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
                Log.i(TAG, "Finished loading list!");
            }
        });
        RefreshingHomeFragment.start();
    }
}