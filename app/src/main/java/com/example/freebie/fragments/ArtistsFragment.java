package com.example.freebie.fragments;

import static com.example.freebie.MainActivity.mainActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.freebie.R;
import com.example.freebie.SongRetrievalService;
import com.example.freebie.adapter.ArtistsAdapter;
import com.example.freebie.models.Artist;

import java.util.ArrayList;

public class ArtistsFragment extends Fragment {

    private static final String TAG = "ArtistsFragment";

    private RecyclerView rvArtists;
    private ProgressBar progressBar;
    private ArrayList<Artist> allArtists;
    private ArtistsAdapter adapter;

    public ArtistsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvArtists = view.findViewById(R.id.rvArtists);
        progressBar = view.findViewById(R.id.progressBar);

        allArtists = new ArrayList<>();
        adapter = new ArtistsAdapter(getContext(), allArtists);

        rvArtists.setAdapter(adapter);
        rvArtists.setLayoutManager(new GridLayoutManager(getContext(), 2));

        progressBar.setVisibility(View.VISIBLE);

        refreshArtists(savedInstanceState);
    }

    public void refreshArtists(Bundle savedInstanceState) {
        Log.i(TAG, "Rebuilding list!");
        // Remember to CLEAR OUT old items before appending in the new ones
        adapter.clear();

        Thread RefreshingHomeFragment = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Just load the current values if nothing from disk is being loaded
                if(!SongRetrievalService.loadingSongs)
                    mainActivity.runOnUiThread(() -> adapter.addAll(Artist.artistArrayList));

                // Check for edge case during configuration change happens during disk load
                if(savedInstanceState != null)
                    return;

                while(SongRetrievalService.loadingSongs) {
                    int startSize = adapter.artists.size();
                    int endSize = Artist.artistArrayList.size();
                    if(startSize < endSize) {
                        mainActivity.runOnUiThread(() -> {
                            for (int i = adapter.artists.size(); i < Artist.artistArrayList.size(); i++) {
                                adapter.add(Artist.artistArrayList.get(i));
                                adapter.notifyItemInserted(i);
                            }
                            if (adapter.artists.size() > 0 && progressBar.getVisibility() == View.VISIBLE)
                                progressBar.setVisibility(View.GONE);
                        });
                    }
                }
                mainActivity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                Log.i(TAG, "Finished loading list with " + adapter.artists.size() + " artists!");
            }
        });
        RefreshingHomeFragment.start();
    }
}