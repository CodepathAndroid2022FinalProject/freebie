package com.example.freebie.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.freebie.R;
import com.example.freebie.models.Artist;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> {

    public static final String TAG = "ArtistsAdapter";

    private Context context;
    public ArrayList<Artist> artists;

    private RequestOptions requestOptions;
    private Drawable placeholderFigure;

    public ArtistsAdapter(Context context, ArrayList<Artist> artists) {
        this.context = context;
        this.artists = artists;

        requestOptions = new RequestOptions();
        requestOptions.transform(new CenterCrop(), new CircleCrop());

        Resources res = context.getResources();
        placeholderFigure = ResourcesCompat.getDrawable(res, android.R.drawable.ic_menu_gallery, null);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.bind(artist);
    }

    @Override
    public int getItemCount() { return artists.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivArtist;
        private TextView tvArtistName;
        private LinearLayout btnArtist;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivArtist = itemView.findViewById(R.id.ivArtist);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            btnArtist = itemView.findViewById(R.id.btnArtist);
        }

        public void bind(Artist artist) {
            tvArtistName.setText(artist.getName());

            btnArtist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Log.i(TAG, "Artist " + artist.getName() + " was pressed!");
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });

            Glide.with(context)
                    .load(artist.getProfilePicture())
                    .apply(requestOptions)
                    .placeholder(placeholderFigure)
                    .error(placeholderFigure)
                    .into(ivArtist);
        }
    }
    // Clean all elements of the recycler
    public void clear() {
        this.artists.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Artist> artists) {
        this.artists.addAll(artists);
        notifyDataSetChanged();
    }

    public void add(Artist artist) { this.artists.add(artist); }
}