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
    private ArrayList<Song> allSongs;
    private SongsAdapter adapter;

    private FragmentManager fragmentManager;

    public static boolean listLoading;

    public HomeFragment() {
        // Required empty public constructor
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

        allSongs = new ArrayList<>();
        adapter = new SongsAdapter(getContext(), allSongs);

        rvSongs.setAdapter(adapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar.setVisibility(View.VISIBLE);

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
                // Somehow, some way, it seems like this is the only way to prevent stuttering
                // on the UI thread. All of the background tasks are moved onto separate threads,
                // but the BottomNavigationView is exceptionally good at stuttering. So I have to
                // forcefully delay the loading of the song list to hold back some processing.
                // If anyone has a better way of doing this, I would be incredibly grateful.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Just load the current values if nothing from disk is being loaded
                if(!SongRetrievalService.loadingSongs)
                    mainActivity.runOnUiThread(() -> adapter.addAll(Song.songArrayList));

                while(SongRetrievalService.loadingSongs) {
                    // Avoid crash if changing tab while refreshing
                    Fragment fragment = fragmentManager.findFragmentByTag(TAG);
                    if(fragment == null) {
                        Log.w(TAG, "Breaking out of thread, fragment switched during loading");
                        return;
                    }

                    int startSize = adapter.songs.size();
                    int endSize = Song.songArrayList.size() - 1;
                    if(startSize < endSize) {
                        mainActivity.runOnUiThread(() -> {
                            for (int i = adapter.songs.size(); i < Song.songArrayList.size(); i++) {
                                adapter.add(Song.songArrayList.get(i));
                                adapter.notifyItemInserted(i);
                            }
                            if (adapter.songs.size() > 0 && progressBar.getVisibility() == View.VISIBLE)
                                progressBar.setVisibility(View.GONE);
                        });
                    }
                }
                mainActivity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                Log.i(TAG, "Finished loading list with " + adapter.songs.size() + " songs!");
            }
        });
        RefreshingHomeFragment.start();
    }
}